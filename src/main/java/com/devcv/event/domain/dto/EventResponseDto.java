package com.devcv.event.domain.dto;

import com.devcv.event.domain.EventResponseEntity;

import java.time.LocalDateTime;

public record EventResponseDto(Long id, Long memberId, Long eventId, String responseJson, LocalDateTime createdDate, LocalDateTime updatedDate) {

    public static EventResponseDto from(EventResponseEntity eventResponseEntity) {
        return new EventResponseDto(eventResponseEntity.getId(),eventResponseEntity.getMemberId(),eventResponseEntity.getEventId(),eventResponseEntity.getResponseJson(),eventResponseEntity.getCreatedDate(),eventResponseEntity.getUpdatedDate());
    }
}