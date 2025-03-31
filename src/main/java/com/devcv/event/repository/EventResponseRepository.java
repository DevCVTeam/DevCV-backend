package com.devcv.event.repository;

import com.devcv.event.domain.EventResponseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventResponseRepository extends JpaRepository<EventResponseEntity, Long> {
}