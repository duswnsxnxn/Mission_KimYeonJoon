package com.ll.gramgram.boundedContext.notification.service;

import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.notification.entity.Notification;
import com.ll.gramgram.boundedContext.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    @Transactional
    public List<Notification> findByToInstaMember(InstaMember toInstaMember) {
        List<Notification> byToInstaMember = notificationRepository.findByToInstaMember(toInstaMember);
        //알림 읽으면 readDate 저장
        byToInstaMember.stream().filter(e -> e.getReadDate() == null)
                .forEach(e -> e.setReadDate(LocalDateTime.now()));
        return notificationRepository.findByToInstaMember(toInstaMember);
    }

    public void likeNotification(LikeablePerson likeablePerson) {
        InstaMember toInstaMember = likeablePerson.getToInstaMember();
        InstaMember fromInstaMember = likeablePerson.getFromInstaMember();

        Notification notification = Notification
                .builder()
                .fromInstaMember(fromInstaMember)
                .toInstaMember(toInstaMember)
                .oldGender(null)
                .newGender(null)
                .oldAttractiveTypeCode(0)
                .newAttractiveTypeCode(0)
                .readDate(null)
                .typeCode("Like")
                .build();

        notificationRepository.save(notification);
    }

    public void modifyNotification(LikeablePerson likeablePerson, int oldAttractiveTypeCode) {
        InstaMember toInstaMember = likeablePerson.getToInstaMember();
        InstaMember fromInstaMember = likeablePerson.getFromInstaMember();

        Notification notification = Notification
                .builder()
                .fromInstaMember(fromInstaMember)
                .toInstaMember(toInstaMember)
                .oldGender(null)
                .newGender(null)
                .oldAttractiveTypeCode(oldAttractiveTypeCode)
                .newAttractiveTypeCode(likeablePerson.getAttractiveTypeCode())
                .readDate(null)
                .typeCode("ModifyAttractiveType")
                .build();

        notificationRepository.save(notification);
    }
}
