package com.devcv.event.domain.dto;

import com.devcv.event.domain.Event;
import com.devcv.event.domain.EventCategory;

import java.time.LocalDateTime;

public record EventResponse(Long eventId, String name, EventCategory eventCategory, Long point,
                            LocalDateTime startDate, LocalDateTime endDate) {

    public static EventResponse from(Event event) {
        return new EventResponse(event.getId(), event.getName(), event.getEventCategory(), event.getPoint(),
                event.getStartDate(), event.getEndDate());
    }
}