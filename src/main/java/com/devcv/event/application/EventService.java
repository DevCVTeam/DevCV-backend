package com.devcv.event.application;

import com.devcv.common.exception.ErrorCode;
import com.devcv.common.exception.NotFoundException;
import com.devcv.event.domain.Event;
import com.devcv.event.domain.dto.EventListResponse;
import com.devcv.event.domain.dto.EventResponse;
import com.devcv.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public Event findByEventId(Long eventId) {
        return eventRepository.findEventByIdAndIsDeletedFalse(eventId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.EVENT_NOT_FOUND));
    }

    public EventResponse getEventResponse(Long eventId) {
        return EventResponse.from(findByEventId(eventId));
    }

    public EventListResponse getEventListResponse() {
        List<EventResponse> eventResponseList = eventRepository.findEventListByIsDeletedFalse()
                .stream()
                .map(EventResponse::from)
                .toList();
        int count = eventResponseList.size();
        return EventListResponse.of(count, eventResponseList);
    }
}