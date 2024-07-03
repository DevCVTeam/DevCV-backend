package com.devcv.order.repository;

import com.devcv.member.domain.Member;
import com.devcv.order.domain.OrderResume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderResumeRepository extends JpaRepository<OrderResume, Long> {

    @Query("SELECT orr " +
            "FROM OrderResume orr " +
            "JOIN FETCH orr.resume r " +
            "JOIN FETCH r.member " +
            "JOIN FETCH r.category " +
            "JOIN FETCH r.imageList " +
            "WHERE orr.order.orderId = :orderId")
    List<OrderResume> findAllByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT orr " +
            "FROM OrderResume orr " +
            "JOIN FETCH orr.order o " +
            "JOIN FETCH orr.resume r " +
            "JOIN FETCH r.member " +
            "JOIN FETCH r.category " +
            "JOIN FETCH r.imageList " +
            "WHERE o.orderNumber = :orderNumber " +
            "AND o.member = :member")
    List<OrderResume> findOrderResumeList(@Param("orderNumber") String orderNumber, @Param("member") Member member);
  
    // 주문id 조회
    @Query("SELECT r FROM OrderResume r " +
            "JOIN r.order o " +
            "WHERE o.member.memberId = :memberId AND r.resume.resumeId = :resumeId")
    List<OrderResume> findByMemberIdAndResumeId(@Param("memberId") Long memberId, @Param("resumeId") Long resumeId);

}