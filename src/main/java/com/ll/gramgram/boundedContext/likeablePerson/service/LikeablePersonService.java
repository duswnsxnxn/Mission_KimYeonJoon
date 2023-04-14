package com.ll.gramgram.boundedContext.likeablePerson.service;

import com.ll.gramgram.base.appConfig.AppConfig;
import com.ll.gramgram.base.rsData.RsData;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.instaMember.service.InstaMemberService;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.likeablePerson.repository.LikeablePersonRepository;
import com.ll.gramgram.boundedContext.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeablePersonService {
    private final LikeablePersonRepository likeablePersonRepository;
    private final InstaMemberService instaMemberService;

    @Transactional
    public RsData<LikeablePerson> like(Member actor, String username, int attractiveTypeCode) {

        RsData checked = canLike(actor, username, attractiveTypeCode);

        if (checked.isFail()) {
            return checked;
        }

        if (checked.getResultCode().equals("S-2")) {
            return updateAttractiveType(username, attractiveTypeCode, checked);
        }

        // 로그인한 사용자가 등록한 인스타 계정
        InstaMember fromInstaMember = actor.getInstaMember();
        // 호감등록 하려는 인스타 계정을 DB로부터 찾아온다.없다면 새로 생성하고 가져온다.
        InstaMember toInstaMember = instaMemberService.findByUsernameOrCreate(username).getData();

        LikeablePerson likeablePerson = LikeablePerson
                .builder()
                .fromInstaMember(fromInstaMember) // 호감을 표시하는 사람의 인스타 멤버
                .fromInstaMemberUsername(actor.getInstaMember().getUsername()) // 중요하지 않음
                .toInstaMember(toInstaMember) // 호감을 받는 사람의 인스타 멤버
                .toInstaMemberUsername(toInstaMember.getUsername()) // 중요하지 않음
                .attractiveTypeCode(attractiveTypeCode) // 1=외모, 2=능력, 3=성격
                .build();

        likeablePersonRepository.save(likeablePerson); // 저장

        toInstaMember.addToLikeablePerson(likeablePerson);

        fromInstaMember.addFromLikeablePerson(likeablePerson);

        return RsData.of("S-1", "입력하신 인스타유저(%s)가 호감상대로 등록되었습니다.".formatted(username), likeablePerson);
    }

    private RsData<LikeablePerson> updateAttractiveType(String username, int attractiveTypeCode, RsData checked) {
        LikeablePerson person = (LikeablePerson) checked.getData();
        String before = person.getAttractiveTypeDisplayName();
        person.setAttractiveTypeCode(attractiveTypeCode);
        String after = person.getAttractiveTypeDisplayName();
        return RsData.of("S-2", "%s에 대한 호감사유를 %s에서 %s으로 변경합니다.".formatted(username, before, after));
    }

    public RsData canLike(Member actor, String username, int attractiveTypeCode) {

        if (actor.hasConnectedInstaMember() == false) {
            return RsData.of("F-1", "먼저 본인의 인스타그램 아이디를 입력해야 합니다.");
        }

        if (actor.getInstaMember().getUsername().equals(username)) {
            return RsData.of("F-2", "본인을 호감상대로 등록할 수 없습니다.");
        }

        List<LikeablePerson> fromList = actor.getInstaMember().getFromLikeablePeople();
        LikeablePerson fromLikeablePerson = null;

        for (int i = 0; i < fromList.size(); i++) {
            InstaMember instaMember = fromList.get(i).getToInstaMember();
            if (instaMember.getUsername().equals(username)) {
                fromLikeablePerson = fromList.get(i);
                break;
            }
        }

        if (fromLikeablePerson != null && fromLikeablePerson.getAttractiveTypeCode() == attractiveTypeCode) {
            return RsData.of("F-3", "중복으로 호감표시를 할 수 없습니다.");
        }

        if (fromLikeablePerson != null) {
            return RsData.of("S-2", "호감 사유를 변경합니다.", fromLikeablePerson);
        }

        long max = AppConfig.getLikeablePersonFromMax();
        if (fromList.size() >= max) {
            return RsData.of("F-4", "최대 %d명까지 호감표시가 가능합니다.".formatted(max));
        }

        return RsData.of("S-1", "호감을 새로 등록합니다.");
    }


    public List<LikeablePerson> findByFromInstaMemberId(Long fromInstaMemberId) {
        return likeablePersonRepository.findByFromInstaMemberId(fromInstaMemberId);
    }

    public Optional<LikeablePerson> findById(Long id) {
        return likeablePersonRepository.findById(id);
    }

    @Transactional
    public RsData delete(LikeablePerson likeablePerson) {

        //엔티티의 FromInstaMembe나 ToInstaMember의 사이즈로 추가 로직 구현할 것을 대비하여 리스트 요소도 처리해줌
        // 너가 생성한 좋아요가 사라졌어.
        likeablePerson.getFromInstaMember().removeFromLikeablePerson(likeablePerson);

        // 너가 받은 좋아요가 사라졌어.
        likeablePerson.getToInstaMember().removeToLikeablePerson(likeablePerson);

        likeablePersonRepository.delete(likeablePerson);

        String likeCanceledUsername = likeablePerson.getToInstaMember().getUsername();
        return RsData.of("S-1", "%s님에 대한 호감을 취소하였습니다.".formatted(likeCanceledUsername));
    }

    public RsData canDelete(Member actor, LikeablePerson likeablePerson) {
        if (likeablePerson == null) return RsData.of("F-1", "이미 삭제되었습니다.");

        // 수행자의 인스타계정 번호
        long actorInstaMemberId = actor.getInstaMember().getId();
        // 삭제 대상의 작성자(호감표시한 사람)의 인스타계정 번호
        long fromInstaMemberId = likeablePerson.getFromInstaMember().getId();

        if (actorInstaMemberId != fromInstaMemberId)
            return RsData.of("F-2", "권한이 없습니다.");

        return RsData.of("S-1", "삭제가능합니다.");
    }


}