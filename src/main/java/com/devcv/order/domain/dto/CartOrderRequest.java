package com.devcv.order.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CartOrderRequest(@Min(value = 1, message = "이력서 개수가 1보다 작습니다.") int resumeCount,
                               @NotNull Long totalPrice,
                               @NotNull @Valid List<CartDto> cartList) {

}