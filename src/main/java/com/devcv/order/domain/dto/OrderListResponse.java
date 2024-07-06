package com.devcv.order.domain.dto;

import java.util.List;

public record OrderListResponse(Long memberId, int orderCount, List<OrderResponse> orderList) {

    public static OrderListResponse of(Long memberId, int orderCount, List<OrderResponse> orderList) {
        return new OrderListResponse(memberId, orderCount, orderList);
    }
}