package com.devcv.order.repository;

import com.devcv.member.domain.Member;
import com.devcv.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, String> {

    Optional<Order> findOrderByOrderNumberAndMember(String orderNumber, Member member);

    List<Order> findOrderListByMember(Member member);

    @Query("SELECT orderResume.resume.resumeId FROM OrderResume orderResume INNER JOIN orderResume.order o WHERE o.member.memberId = :memberId")
    List<Long> getResumeIdsByMemberId(@Param("memberId") Long memberId);
}