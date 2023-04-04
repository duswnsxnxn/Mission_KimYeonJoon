package com.ll.gramgram.boundedContext.likeablePerson.entity;

import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@ToString
@Entity
@Getter
public class LikeablePerson {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @CreatedDate
    private LocalDateTime createDate;
    @LastModifiedDate
    private LocalDateTime modifyDate;

    @ManyToOne
    private InstaMember fromInstaMember; // 호감을 표시한 사람(인스타 멤버)
    private String fromInstaMemberUsername; // 혹시 몰라서 기록
    @ManyToOne(cascade = CascadeType.REMOVE) // 호감인 사람을 호감리스트에서 삭제 시 연관된 InstaMember 엔티티도 함께 삭제하기 위해 영속성 전이 속성을 주었다.
    private InstaMember toInstaMember; // 호감을 받은 사람(인스타 멤버)
    private String toInstaMemberUsername; // 혹시 몰라서 기록
    private int attractiveTypeCode; // 매력포인트(1=외모, 2=성격, 3=능력)

    public String getAttractiveTypeDisplayName() {
        return switch (attractiveTypeCode) {
            case 1 -> "외모";
            case 2 -> "성격";
            default -> "능력";
        };
    }
}
