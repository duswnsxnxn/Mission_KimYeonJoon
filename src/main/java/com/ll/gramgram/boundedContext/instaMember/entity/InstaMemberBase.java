package com.ll.gramgram.boundedContext.instaMember.entity;

import com.ll.gramgram.base.baseEntity.BaseEntity;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@Getter
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public abstract class InstaMemberBase extends BaseEntity {
    @Setter
    String gender;
    //OrderSpecifier의 Expression으로 이용하여 정렬 조건에 만족시키기 위해 컬럼 추가
    long likes;

    long likesCountByGenderWomanAndAttractiveTypeCode1;
    long likesCountByGenderWomanAndAttractiveTypeCode2;
    long likesCountByGenderWomanAndAttractiveTypeCode3;
    long likesCountByGenderManAndAttractiveTypeCode1;
    long likesCountByGenderManAndAttractiveTypeCode2;
    long likesCountByGenderManAndAttractiveTypeCode3;

    public Long getLikesCountByGenderWoman() {
        return likesCountByGenderWomanAndAttractiveTypeCode1 + likesCountByGenderWomanAndAttractiveTypeCode2 + likesCountByGenderWomanAndAttractiveTypeCode3;
    }

    public Long getLikesCountByGenderMan() {
        return likesCountByGenderManAndAttractiveTypeCode1 + likesCountByGenderManAndAttractiveTypeCode2 + likesCountByGenderManAndAttractiveTypeCode3;
    }

    public Long getLikesCountByAttractionTypeCode1() {
        return likesCountByGenderWomanAndAttractiveTypeCode1 + likesCountByGenderManAndAttractiveTypeCode1;
    }

    public Long getLikesCountByAttractionTypeCode2() {
        return likesCountByGenderWomanAndAttractiveTypeCode2 + likesCountByGenderManAndAttractiveTypeCode2;
    }

    public Long getLikesCountByAttractionTypeCode3() {
        return likesCountByGenderWomanAndAttractiveTypeCode3 + likesCountByGenderManAndAttractiveTypeCode3;
    }

    public Long getLikes() {
        return getLikesCountByGenderWoman() + getLikesCountByGenderMan();
    }
}
