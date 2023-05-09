package com.ll.gramgram.boundedContext.likeablePerson.repository;

import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import static com.ll.gramgram.boundedContext.likeablePerson.entity.QLikeablePerson.likeablePerson;

@RequiredArgsConstructor
public class LikeablePersonRepositoryImpl implements LikeablePersonRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<LikeablePerson> findQslByFromInstaMemberIdAndToInstaMember_username(long fromInstaMemberId, String toInstaMemberUsername) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .selectFrom(likeablePerson)
                        .where(
                                likeablePerson.fromInstaMember.id.eq(fromInstaMemberId)
                                        .and(
                                                likeablePerson.toInstaMember.username.eq(toInstaMemberUsername)
                                        )
                        )
                        .fetchOne()
        );
    }

    @Override
    public List<LikeablePerson> findQslByGenderAndAttractiveTypeCode(InstaMember instaMember, String gender, String attractiveTypeCode) {
        return
                jpaQueryFactory
                        .selectFrom(likeablePerson)
                        .where(eqInsta(instaMember), eqGender(gender), eqAttractiveTypeCode(instaMember, attractiveTypeCode))
                        .fetch();
    }

    private BooleanExpression eqInsta(InstaMember instaMember) {
        if(instaMember == null) {
            return null;
        }
        return likeablePerson.toInstaMember.eq(instaMember);
    }
    private BooleanExpression eqGender(String gender) {
        if(gender == null || gender.isEmpty()) {
            return null;
        }
        return likeablePerson.fromInstaMember.gender.eq(gender);
    }

    private BooleanExpression eqAttractiveTypeCode(InstaMember instaMember, String attractiveTypeCode) {
        if(attractiveTypeCode == null || attractiveTypeCode.isEmpty()) {
            return null;
        }
        return likeablePerson.attractiveTypeCode.eq(Integer.valueOf(attractiveTypeCode));
    }


}
