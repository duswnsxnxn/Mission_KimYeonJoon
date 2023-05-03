package com.ll.gramgram.boundedContext.notification.service;

import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.likeablePerson.service.LikeablePersonService;
import com.ll.gramgram.boundedContext.member.entity.Member;
import com.ll.gramgram.boundedContext.member.service.MemberService;
import com.ll.gramgram.boundedContext.notification.entity.Notification;
import com.ll.gramgram.boundedContext.notification.repository.NotificationRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.MethodName.class)
class NotificationServiceTests {
    @Autowired
    private MemberService memberService;
    @Autowired
    private LikeablePersonService likeablePersonService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private NotificationRepository notificationRepository;


    @DisplayName("본인에 대한 누군가의 호감 표시, 수정 알림 리스트")
    @Test
    public void t1() throws Exception {
        // NotProd가 실행되어 저장되있는 user3이름으로 된 member를 가져옴
        Member user3 = memberService.findByUsername("user3").orElseThrow();
        // NotProd가 실행되면서 user3의 인스타 계정인 insta_user3는 insta_user4에게 호감 표시를 자동으로 해준다.
        // 이 부분에서 insta_user4에 대한 알림리스트에 1 추가된다.
        // 자동으로 호감 표시가 되었으므로 해당되는 likeablePerson을 조회한다.
        Optional<LikeablePerson> likeablePerson = likeablePersonService.findByFromInstaMember_usernameAndToInstaMember_username("insta_user3", "insta_user4");
        // 조회한 likeablePerson의 호감 사유를 수정함으로써 insta_user4에 대한 알림리스트에 1 추가되어 총 리스트 길이는 2가 된다.
        likeablePersonService.modifyAttractive(user3, likeablePerson.get(), 2);
        Member memberUser4 = memberService.findByUsername("user4").orElseThrow();
        InstaMember instaMember = memberUser4.getInstaMember();
        // 찾아온 인스타 멤버를 기준으로 알림 리스트를 조회한다.
        List<Notification> byToInstaMember = notificationService.findByToInstaMember(instaMember);
        // NotProd가 실행되면서 호감 표시로 + 1
        // 테스트 케이스에서 modifyAttractive함으로써 + 1
        // 총 알림이 2개 발생하였으므로 2가 나와야한다.
        Assertions.assertThat(byToInstaMember.size()).isEqualTo(2);
    }

    @DisplayName("알림을 읽으면 readDate가 지금시간으로 업데이트")
    @Test
    public void t2() throws Exception {
        // user4의 인스타 멤버 조회
        Member memberUser4 = memberService.findByUsername("user4").orElseThrow();
        InstaMember insta_user4 = memberUser4.getInstaMember();
        // notificationService내에는 날짜 업데이트 로직이 있기 때문에 거치지 않고 레포지토리로부터 조회
        List<Notification> notification = notificationRepository.findByToInstaMember(insta_user4);
        Assertions.assertThat(notification.get(0).getReadDate()).isSameAs(null);
        // 찾아온 인스타 멤버를 기준으로 알림 리스트를 조회하면 내부 로직에 의해 null이였던 readDate가 지금 시간으로 업데이트 됨.
        List<Notification> byToInstaMember = notificationService.findByToInstaMember(insta_user4);
        // insta_user4의 알림 중 첫번 째만 조회
        Notification firstNotification = byToInstaMember.get(0);
        //getReadDateStr메소드는 LocalDateTime타입인 readDate를 HH시:mm분, 0분 전 양식으로 반환해준다.
        String readDateStr = firstNotification.getReadDateStr();
        //10시 11분, 0분 전 처럼 나오는지 확인 테스트
        Assertions.assertThat(readDateStr)
        .isEqualTo(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH시:mm분, 0분 전")));
    }
}