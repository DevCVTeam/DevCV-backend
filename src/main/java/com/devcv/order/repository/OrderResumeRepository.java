package com.devcv.order.repository;

import com.devcv.member.domain.Member;
import com.devcv.order.domain.OrderResume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderResumeRepository extends JpaRepository<OrderResume, Long> {

    @Query("SELECT o FROM OrderResume o JOIN FETCH o.resume i JOIN FETCH i.imageList WHERE o.order.orderId = :orderId")
    List<OrderResume> findAllByOrder_OrderId(@Param("orderId") Long orderId);

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
}