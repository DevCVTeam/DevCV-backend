package com.devcv.event.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record EventRequest(@NotNull String name,
                           @NotNull String eventCategory,
                           @NotNull @Positive Long point,
                           @NotNull LocalDateTime startDate,
                           @NotNull LocalDateTime endDate) {

}