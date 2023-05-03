package com.ll.gramgram.boundedContext.notification.service;

import com.ll.gramgram.base.appConfig.AppConfig;
import com.ll.gramgram.boundedContext.likeablePerson.service.LikeablePersonService;
import com.ll.gramgram.boundedContext.member.entity.Member;
import com.ll.gramgram.boundedContext.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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


    @DisplayName("본인에 대한 누군가의 호감 표시, 수정 알림 리스트")
    @Test
    public void t1() throws Exception {
        // given
        // NotProd가 실행되어 저장되있는 user3이름으로 된 member를 가져와서
        Member memberUser3 = memberService.findByUsername("user3").orElseThrow();
        // when
        // bts이름을 가진 인스타 계정에 호감 표시를 하고
        likeablePersonService.like(memberUser3, "bts", 3).getMsg();
        // 호감 수정을 바로 하려고 하면 쿨타임 예외처리 로직에 검출되어 예외 메세지를 반환받는다.
        String RsMsg = likeablePersonService.like(memberUser3, "bts", 2).getMsg();
        // when
    
        // then
    }
}