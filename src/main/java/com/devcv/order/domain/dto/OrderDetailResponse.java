package com.devcv.order.domain.dto;

import com.devcv.order.domain.Order;
import com.devcv.order.domain.OrderStatus;
import com.devcv.resume.domain.dto.ResumeDto;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResponse(String orderNumber,
                                  Long totalPrice,
                                  OrderStatus orderStatus,
                                  LocalDateTime createdDate,
                                  List<ResumeDto> resumeList) {

    public static OrderDetailResponse of(Order order, List<ResumeDto> resumeList) {
        return new OrderDetailResponse(
                order.getOrderNumber(),
                order.getTotalPrice(),
                order.getOrderStatus(),
                order.getCreatedDate(),
                resumeList);
    }
}