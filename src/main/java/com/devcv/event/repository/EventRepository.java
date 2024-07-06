package com.devcv.event.repository;

import com.devcv.event.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findEventByIdAndIsDeletedFalse(Long eventId);

    List<Event> findEventListByIsDeletedFalse();
}