package com.devcv.order.repository;

import com.devcv.order.domain.OrderResume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderResumeRepository extends JpaRepository<OrderResume, Long> {

    @Query("SELECT o FROM OrderResume o JOIN FETCH o.resume i JOIN FETCH i.imageList WHERE o.order.orderId = :orderId")
    List<OrderResume> findAllByOrder_OrderId(@Param("orderId") Long orderId);


    // 주문id 조회
    @Query("SELECT r FROM OrderResume r " +
            "JOIN r.order o " +
            "WHERE o.member.memberId = :memberId AND r.resume.resumeId = :resumeId")
    List<OrderResume> findByMemberIdAndResumeId(@Param("memberId") Long memberId, @Param("resumeId") Long resumeId);
}