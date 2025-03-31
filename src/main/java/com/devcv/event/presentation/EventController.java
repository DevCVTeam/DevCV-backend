package com.devcv.event.presentation;

import com.devcv.event.application.EventService;
import com.devcv.event.domain.dto.EventListResponse;
import com.devcv.event.domain.dto.EventResponse;
import com.devcv.event.domain.dto.EventResponseDto;
import com.devcv.event.domain.dto.EventResponseRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<EventListResponse> getEventList() {
        EventListResponse response = eventService.getEventListResponse();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{event-id}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable("event-id") Long eventId) {
        EventResponse response = eventService.getEventResponse(eventId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{event-id}/answer")
    public ResponseEntity<EventResponseDto> doEventResponse(@PathVariable("event-id") Long eventId, @RequestBody EventResponseRequest request) {
        EventResponseDto response = eventService.saveOrUpdateResponse(eventId, request);
        return ResponseEntity.ok(response);
    }
}