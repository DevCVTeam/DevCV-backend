package com.devcv.order.domain.dto;

import jakarta.validation.constraints.NotNull;

public record CartDto(@NotNull Long resumeId, @NotNull Long price) {

}