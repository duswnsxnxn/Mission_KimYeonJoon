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
    public RsData<LikeablePerson> like(Member member, String username, int attractiveTypeCode) {
        if (member.hasConnectedInstaMember() == false) {
            return RsData.of("F-2", "먼저 본인의 인스타그램 아이디를 입력해야 합니다.");
        }

        if (member.getInstaMember().getUsername().equals(username)) {
            return RsData.of("F-1", "본인을 호감상대로 등록할 수 없습니다.");
        }
        // 로그인한 사용자가 등록한 인스타 계정
        InstaMember instaMember = member.getInstaMember();
        // 호감등록 하려는 인스타 계정을 DB로부터 찾아온다.없다면 새로 생성하고 가져온다.
        InstaMember toInstaMember = instaMemberService.findByUsernameOrCreate(username).getData();
        // 중복 호감등록 예외 검사 서비스인 canActorLike로 부터 결과 데이터를 받는다.
        RsData canActorLikeRsData = canActorLike(instaMember, toInstaMember, attractiveTypeCode);
        // 중복검사에 실패한 경우
        if (canActorLikeRsData.isFail()) {
            return canActorLikeRsData;
        }
        // 중복검사에 통과한 경우 필터링 된 LikeablePerson을 받는다.
        LikeablePerson compare = (LikeablePerson) canActorLikeRsData.getData();
        // 중복검사 로직에는 호감등록 하려는 인스타 계정이 같더라도 호감사유가 다르면 LikeablePerson을 반환한다.
        // 따라서 null이 아닌 제대로 반환이 되었다는 뜻은 기존에 등록했던 인스타 계정의 호감사유만 변경하겠다는 것이다.
        // null이라면 save를 통해 새로 만듬
        if (compare != null) {
            String before = compare.getAttractiveTypeDisplayName();
            compare.update(attractiveTypeCode);
            String after = compare.getAttractiveTypeDisplayName();
            return RsData.of("S-2", "%s에 대한 호감사유를 %s에서 %s으로 변경합니다.".formatted(username, before, after));
        }
        // save하기 전에 먼저 DB로부터 로그인한 인스타 계정의 호감 표시 목록을 전부 가져온다.
        List<LikeablePerson> list = findByFromInstaMemberId(instaMember.getId());
        // 11명 이상 호감등록을 할 수 없게끔 가져온 목록의 사이즈가 조건식을 만족하는지 검사한다.
        // 11명 이상인데 조건식에서는 (list.size() > 9) 이렇게 설정한 이유는 사이즈를 먼저 비교하고 save하는 로직이기 때문이다.
        // 만약 사용자가 10번째 호감등록을 하려고 할때 목록의 사이즈는 9인 상태일 것이고 9보다 커지지는 않은 상태이니 조건문을 통과하고
        // save한다. 그리고 다시 1명을 더 호감등록 한다면 목록 사이즈는 10이고 조건식을 만족하지 못해 입구컷 날 것이다.
        long max = AppConfig.getLikeablePersonFromMax();
        if (list.size() > max) {
            return RsData.of("F-1", "11명 이상의 호감상대를 등록 할 수 없습니다.");
        }

        LikeablePerson likeablePerson = LikeablePerson
                .builder()
                .fromInstaMember(instaMember) // 호감을 표시하는 사람의 인스타 멤버
                .fromInstaMemberUsername(member.getInstaMember().getUsername()) // 중요하지 않음
                .toInstaMember(toInstaMember) // 호감을 받는 사람의 인스타 멤버
                .toInstaMemberUsername(toInstaMember.getUsername()) // 중요하지 않음
                .attractiveTypeCode(attractiveTypeCode) // 1=외모, 2=능력, 3=성격
                .build();

        likeablePersonRepository.save(likeablePerson); // 저장

        // 너가 좋아하는 호감표시 생겼어.
        instaMember.addFromLikeablePerson(likeablePerson);

        // 너를 좋아하는 호감표시 생겼어.
        toInstaMember.addToLikeablePerson(likeablePerson);

        return RsData.of("S-1", "입력하신 인스타유저(%s)를 호감상대로 등록되었습니다.".formatted(username), likeablePerson);
    }

    public RsData canActorLike(InstaMember fromInsta, InstaMember toInsta, int attractiveTypeCode) {
        Long fromInstaId = fromInsta.getId();
        Long toInstaId = toInsta.getId();
        Optional<LikeablePerson> likeablePerson = likeablePersonRepository.findByFromInstaMemberIdAndToInstaMemberId(fromInstaId, toInstaId);
        int getAttractive = 0;
        LikeablePerson person = null;
        if (likeablePerson.isPresent()) {
            person = likeablePerson.get();
            getAttractive = person.getAttractiveTypeCode();
            if (getAttractive == attractiveTypeCode) {
                return RsData.of("F-2", "중복으로 호감표시를 할 수 없습니다.");
            }
        }

        // DB에 인스타 유저가 없다면 null을 반환
        return RsData.of("S-1", "가능합니다.", person);
    }


    public List<LikeablePerson> findByFromInstaMemberId(Long fromInstaMemberId) {
        return likeablePersonRepository.findByFromInstaMemberId(fromInstaMemberId);
    }

    public Optional<LikeablePerson> findById(Long id) {
        return likeablePersonRepository.findById(id);
    }

    @Transactional
    public RsData delete(LikeablePerson likeablePerson) {
        likeablePersonRepository.delete(likeablePerson);

        String likeCanceledUsername = likeablePerson.getToInstaMember().getUsername();
        return RsData.of("S-1", "%s님에 대한 호감을 취소하였습니다.".formatted(likeCanceledUsername));
    }

    public RsData canActorDelete(Member actor, LikeablePerson likeablePerson) {
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