package com.ll.gramgram.boundedContext.likeablePerson.repository;

import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
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
    public List<LikeablePerson> findQslByGenderAndAttractiveTypeCode(InstaMember instaMember, String gender, int attractiveTypeCode, int sortCode) {
        return
                jpaQueryFactory
                        .selectFrom(likeablePerson)
                        .where(eqInsta(instaMember), eqGender(gender), eqAttractiveTypeCode(instaMember, attractiveTypeCode))
                        .orderBy(sortByField(sortCode))
                        .fetch();
    }

    private BooleanExpression eqInsta(InstaMember instaMember) {
        if(instaMember == null) {
            return null;
        }
        return likeablePerson.toInstaMember.eq(instaMember);
    }
    private BooleanExpression eqGender(String gender) {
        if(gender.isEmpty()) {
            return null;
        }
        return likeablePerson.fromInstaMember.gender.eq(gender);
    }

    private BooleanExpression eqAttractiveTypeCode(InstaMember instaMember, int attractiveTypeCode) {
        if(attractiveTypeCode == 0) {
            return null;
        }
        return likeablePerson.attractiveTypeCode.eq(attractiveTypeCode);
    }

    private OrderSpecifier[] sortByField(int sortCode) {

        List<OrderSpecifier> orderSpecifiers = new ArrayList<>();


        switch (sortCode) {
            case 1 -> orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, likeablePerson.createDate));
            case 2 -> orderSpecifiers.add(new OrderSpecifier<>(Order.ASC, likeablePerson.createDate));
            case 3 -> {
                orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, likeablePerson.fromInstaMember.likes));
                orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, likeablePerson.createDate));
            }
            case 4 -> {
                orderSpecifiers.add(new OrderSpecifier<>(Order.ASC, likeablePerson.fromInstaMember.likes));
                orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, likeablePerson.createDate));
            }
            case 5 -> {
                orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, likeablePerson.fromInstaMember.gender));
                orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, likeablePerson.createDate));
            }
            case 6 -> {
                orderSpecifiers.add(new OrderSpecifier<>(Order.ASC, likeablePerson.attractiveTypeCode));
                orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, likeablePerson.createDate));
            }
        }
        //다중 정렬 조건을 고려하여 배열로 반환
        return orderSpecifiers.toArray(new OrderSpecifier[orderSpecifiers.size()]);
    }
}
