package com.devcv.order.domain;

import lombok.Getter;

@Getter
public enum OrderStatus {

    PENDING("결제대기"),
    COMPLETED("결제완료"),
    CANCELLED("취소");

    private final String status;

    OrderStatus(String status){
        this.status = status;
    }
}