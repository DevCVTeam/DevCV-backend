package com.devcv.admin.dto;

import com.devcv.event.domain.Event;

import java.time.LocalDateTime;

public record DeletedEventResponse(Long eventId, String eventName, Boolean isDeleted, LocalDateTime deletedDate) {

    public static DeletedEventResponse of(Event event) {
        return new DeletedEventResponse(event.getId(), event.getName(), event.getIsDeleted(), event.getDeletedDate());
    }
}