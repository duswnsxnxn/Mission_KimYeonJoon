package com.ll.gramgram.boundedContext.likeablePerson.service;


import com.ll.gramgram.TestUt;
import com.ll.gramgram.base.appConfig.AppConfig;
import com.ll.gramgram.base.rsData.RsData;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.instaMember.service.InstaMemberService;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.likeablePerson.repository.LikeablePersonRepository;
import com.ll.gramgram.boundedContext.member.entity.Member;
import com.ll.gramgram.boundedContext.member.service.MemberService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.MethodName.class)
public class LikeablePersonServiceTests {
    @Autowired
    private MemberService memberService;
    @Autowired
    private InstaMemberService instaMemberService;
    @Autowired
    private LikeablePersonService likeablePersonService;
    @Autowired
    private LikeablePersonRepository likeablePersonRepository;

    @Test
    @DisplayName("테스트 1")
    void t001() throws Exception {
        // 2번 좋아요 정보를 가져온다.
        /*
        SELECT *
        FROM likeable_person
        WHERE id = 2;
        */
        LikeablePerson likeablePersonId2 = likeablePersonService.findById(2L).get();

        // 2번 좋아요를 발생시킨(호감을 표시한) 인스타회원을 가져온다.
        // 그 회원의 인스타아이디는 insta_user3 이다.
        /*
        SELECT *
        FROM insta_member
        WHERE id = 2;
        */
        InstaMember instaMemberInstaUser3 = likeablePersonId2.getFromInstaMember();
        assertThat(instaMemberInstaUser3.getUsername()).isEqualTo("insta_user3");

        // 인스타아이디가 insta_user3 인 사람이 호감을 표시한 `좋아요` 목록
        // 좋아요는 2가지로 구성되어 있다 : from(호감표시자), to(호감받은자)
        /*
        SELECT *
        FROM likeable_person
        WHERE from_insta_member_id = 2;
        */
        List<LikeablePerson> fromLikeablePeople = instaMemberInstaUser3.getFromLikeablePeople();

        // 특정 회원이 호감을 표시한 좋아요 반복한다.
        for (LikeablePerson likeablePerson : fromLikeablePeople) {
            // 당연하게 그 특정회원(인스타아이디 instal_user3)이 좋아요의 호감표시자회원과 같은 사람이다.
            assertThat(instaMemberInstaUser3.getUsername()).isEqualTo(likeablePerson.getFromInstaMember().getUsername());
        }
    }

    @Test
    @DisplayName("테스트 2")
    void t002() throws Exception {
        // 2번 좋아요 정보를 가져온다.
        /*
        SELECT *
        FROM likeable_person
        WHERE id = 2;
        */
        LikeablePerson likeablePersonId2 = likeablePersonService.findById(2L).get();

        // 2번 좋아요를 발생시킨(호감을 표시한) 인스타회원을 가져온다.
        // 그 회원의 인스타아이디는 insta_user3 이다.
        /*
        SELECT *
        FROM insta_member
        WHERE id = 2;
        */
        InstaMember instaMemberInstaUser3 = likeablePersonId2.getFromInstaMember();
        assertThat(instaMemberInstaUser3.getUsername()).isEqualTo("insta_user3");

        // 내가 새로 호감을 표시하려는 사람의 인스타 아이디
        String usernameToLike = "insta_user4";

        // v1
        LikeablePerson likeablePersonIndex0 = instaMemberInstaUser3.getFromLikeablePeople().get(0);
        LikeablePerson likeablePersonIndex1 = instaMemberInstaUser3.getFromLikeablePeople().get(1);

        if (usernameToLike.equals(likeablePersonIndex0.getToInstaMember().getUsername())) {
            System.out.println("v1 : 이미 나(인스타아이디 : insta_user3)는 insta_user4에게 호감을 표시 했구나.");
        }

        if (usernameToLike.equals(likeablePersonIndex1.getToInstaMember().getUsername())) {
            System.out.println("v1 : 이미 나(인스타아이디 : insta_user3)는 insta_user4에게 호감을 표시 했구나.");
        }

        // v2
        for (LikeablePerson fromLikeablePerson : instaMemberInstaUser3.getFromLikeablePeople()) {
            String toInstaMemberUsername = fromLikeablePerson.getToInstaMember().getUsername();

            if (usernameToLike.equals(toInstaMemberUsername)) {
                System.out.println("v2 : 이미 나(인스타아이디 : insta_user3)는 insta_user4에게 호감을 표시 했구나.");
                break;
            }
        }

        // v3
        long count = instaMemberInstaUser3
                .getFromLikeablePeople()
                .stream()
                .filter(lp -> lp.getToInstaMember().getUsername().equals(usernameToLike))
                .count();

        if (count > 0) {
            System.out.println("v3 : 이미 나(인스타아이디 : insta_user3)는 insta_user4에게 호감을 표시 했구나.");
        }

        // v4
        LikeablePerson oldLikeablePerson = instaMemberInstaUser3
                .getFromLikeablePeople()
                .stream()
                .filter(lp -> lp.getToInstaMember().getUsername().equals(usernameToLike))
                .findFirst()
                .orElse(null);

        if (oldLikeablePerson != null) {
            System.out.println("v4 : 이미 나(인스타아이디 : insta_user3)는 insta_user4에게 호감을 표시 했구나.");
            System.out.printf("v4 : 기존 호감사유 : %s%n", oldLikeablePerson.getAttractiveTypeDisplayName());
        }
    }

    @Test
    @DisplayName("설정파일에 있는 최대가능호감표시 수 가져오기")
    void t003() throws Exception {
        long likeablePersonFromMax = AppConfig.getLikeablePersonFromMax();

        assertThat(likeablePersonFromMax).isEqualTo(10);
    }

    @Test
    @DisplayName("테스트 4")
    void t004() throws Exception {
        // 좋아하는 사람이 2번 인스타 회원인 `좋아요` 검색
        /*
        SELECT l1_0.id,
        l1_0.attractive_type_code,
        l1_0.create_date,
        l1_0.from_insta_member_id,
        l1_0.from_insta_member_username,
        l1_0.modify_date,
        l1_0.to_insta_member_id,
        l1_0.to_insta_member_username
        FROM likeable_person l1_0
        WHERE l1_0.from_insta_member_id = 2
        */
        List<LikeablePerson> likeablePeople = likeablePersonRepository.findByFromInstaMemberId(2L);

        // 좋아하는 대상의 아이디가 insta_user100 인 `좋아요`들 만 검색
        /*
        SELECT l1_0.id,
        l1_0.attractive_type_code,
        l1_0.create_date,
        l1_0.from_insta_member_id,
        l1_0.from_insta_member_username,
        l1_0.modify_date,
        l1_0.to_insta_member_id,
        l1_0.to_insta_member_username
        FROM likeable_person l1_0
        LEFT JOIN insta_member t1_0
        ON t1_0.id=l1_0.to_insta_member_id
        WHERE t1_0.username = "insta_user100";
        */
        List<LikeablePerson> likeablePeople2 = likeablePersonRepository.findByToInstaMember_username("insta_user100");

        assertThat(likeablePeople2.get(0).getId()).isEqualTo(2);

        // 좋아하는 사람이 2번 인스타 회원이고, 좋아하는 대상의 인스타아이디가 "insta_user100" 인 `좋아요`
        /*
        SELECT l1_0.id,
        l1_0.attractive_type_code,
        l1_0.create_date,
        l1_0.from_insta_member_id,
        l1_0.from_insta_member_username,
        l1_0.modify_date,
        l1_0.to_insta_member_id,
        l1_0.to_insta_member_username
        FROM likeable_person l1_0
        LEFT JOIN insta_member t1_0
        ON t1_0.id=l1_0.to_insta_member_id
        WHERE l1_0.from_insta_member_id = 2
        AND t1_0.username = "insta_user100";
        */
        LikeablePerson likeablePerson = likeablePersonRepository.findByFromInstaMemberIdAndToInstaMember_username(2L, "insta_user100");

        assertThat(likeablePerson.getId()).isEqualTo(2);
    }

    @Test
    @DisplayName("테스트 5")
    void t005() throws Exception {
        LikeablePerson likeablePerson = likeablePersonRepository.findQslByFromInstaMemberIdAndToInstaMember_username(2L, "insta_user4").orElse(null);

        assertThat(likeablePerson.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("설정파일에서 호감표시에 대한 수정쿨타임 가져오기")
    void t006() throws Exception {
        System.out.println("likeablePersonModifyCoolTime : " + AppConfig.getLikeablePersonModifyCoolTime());
        assertThat(AppConfig.getLikeablePersonModifyCoolTime()).isGreaterThan(0);
    }

    @Test
    @DisplayName("호감표시를 하면 쿨타임이 지정된다.")
    void t007() throws Exception {
        // 현재시점 기준에서 쿨타임이 다 차는 시간을 구한다.(미래)
        LocalDateTime coolTime = AppConfig.genLikeablePersonModifyUnlockDate();

        Member memberUser3 = memberService.findByUsername("user3").orElseThrow();
        // 호감표시를 생성한다.
        // // 호감표시를 생성하면 쿨타임이 미래로 지정된다.
        LikeablePerson likeablePersonToBts = likeablePersonService.like(memberUser3, "bts", 3).getData();

        // 잘 지정되었는지
        assertThat(
                likeablePersonToBts.getModifyUnlockDate().isAfter(coolTime)
        ).isTrue();
    }

    @Test
    @DisplayName("호감사유를 변경하면 쿨타임이 갱신된다.")
    void t008() throws Exception {
        // 현재시점 기준에서 쿨타임이 다 차는 시간을 구한다.(미래)
        LocalDateTime coolTime = AppConfig.genLikeablePersonModifyUnlockDate();

        Member memberUser3 = memberService.findByUsername("user3").orElseThrow();
        // 호감표시를 생성한다.
        LikeablePerson likeablePersonToBts = likeablePersonService.like(memberUser3, "bts", 3).getData();

        // 호감표시를 생성하면 쿨타임이 지정되기 때문에, 그래서 바로 수정이 안된다.
        // 그래서 강제로 쿨타임이 지난것으로 만든다.
        // 테스트를 위해서 억지로 값을 넣는다.
        TestUt.setFieldValue(likeablePersonToBts, "modifyUnlockDate", LocalDateTime.now().minusSeconds(1));

        // 수정을 하면 쿨타임이 갱신된다.
        likeablePersonService.modifyAttractive(memberUser3, likeablePersonToBts, 1);

        // 갱신 되었는지 확인
        assertThat(
                likeablePersonToBts.getModifyUnlockDate().isAfter(coolTime)
        ).isTrue();
    }
    
//    @DisplayName("toList에서 성별 필터링기능 구현")
//    @Test
//    void t009() throws Exception {
//
//        Member memberUser1 = memberService.join("user1", "1234").getData();
//        Member memberUser2 = memberService.join("user2", "1234").getData();
//        Member memberUser3 = memberService.join("user3", "1234").getData();
//        Member memberUser4 = memberService.join("user4", "1234").getData();
//        InstaMember instaMember1 = instaMemberService.connect(memberUser1, "insta_user1", "W").getData();
//        InstaMember instaMember2 = instaMemberService.connect(memberUser2, "insta_user2", "M").getData();
//        InstaMember instaMember3 = instaMemberService.connect(memberUser3, "insta_user3", "W").getData();
//        InstaMember instaMember4 = instaMemberService.connect(memberUser4, "insta_user4", "W").getData();
//        // memberUser2~4(insta_user2~4)는 insta_user1을 호감표시함
//        likeablePersonService.like(memberUser2, "insta_user1", 1).getData();
//        likeablePersonService.like(memberUser3, "insta_user1", 2).getData();
//        likeablePersonService.like(memberUser4, "insta_user1", 3).getData();
//        // insta_user1이 받은 호감 리스트를 남자만 필터링하여 불러온다.
//        List<LikeablePerson> likeByCondition = likeablePersonService.findToLikeByCondition(instaMember1, "M", "", "").getData();
//        // 필터링이 잘 되었다면 리스트 중 첫번 째 목록의 성별이 남자일 것이다.
//        Assertions
//                .assertThat(likeByCondition.get(0).getFromInstaMember().getGender())
//                .isEqualTo("M");
//    }
}
