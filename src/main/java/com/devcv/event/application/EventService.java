package com.devcv.event.application;

import com.devcv.common.exception.ErrorCode;
import com.devcv.common.exception.NotFoundException;
import com.devcv.event.domain.Event;
import com.devcv.event.domain.EventResponseEntity;
import com.devcv.event.domain.dto.EventListResponse;
import com.devcv.event.domain.dto.EventResponse;
import com.devcv.event.domain.dto.EventResponseDto;
import com.devcv.event.domain.dto.EventResponseRequest;
import com.devcv.event.repository.EventRepository;
import com.devcv.event.repository.EventResponseRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventResponseRepository eventResponseRepository;
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

    @Transactional
    public EventResponseDto saveOrUpdateResponse(Long eventId, EventResponseRequest request) {
        EventResponseEntity newResponse = new EventResponseEntity();
        newResponse.setMemberId(request.getMemberId());
        newResponse.setEventId(eventId);
        newResponse.setResponseJson(request.getResponseJson());

        EventResponseEntity res = eventResponseRepository.save(newResponse);
        return EventResponseDto.from(res);
    }
}