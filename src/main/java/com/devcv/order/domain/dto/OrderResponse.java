package com.devcv.order.domain.dto;

import com.devcv.order.domain.Order;
import com.devcv.order.domain.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;


public record OrderResponse(String orderNumber,
                            Long totalPrice,
                            OrderStatus orderStatus,
                            LocalDateTime createdDate,
                            List<OrderResumeDto> resumeList) {

    public static OrderResponse of(Order order, List<OrderResumeDto> orderResumeDtoList) {
        return new OrderResponse(
                order.getOrderNumber(),
                order.getTotalPrice(),
                order.getOrderStatus(),
                order.getCreatedDate(),
                orderResumeDtoList);
    }
}