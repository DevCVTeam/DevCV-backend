package com.devcv.order.domain.dto;

import com.devcv.order.domain.Order;
import com.devcv.order.domain.OrderStatus;
import com.devcv.order.domain.PayType;

import java.time.LocalDateTime;


public record OrderResponse(String orderId,
                            String resumeTitle,
                            int totalAmount,
                            OrderStatus orderStatus,
                            LocalDateTime createdDate,
                            PayType payType,
                            String sellerName) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getOrderId(),
                order.getResume().getTitle(),
                order.getTotalAmount(),
                order.getOrderStatus(),
                order.getCreatedDate(),
                order.getPayType(),
                order.getSellerName());
    }
}