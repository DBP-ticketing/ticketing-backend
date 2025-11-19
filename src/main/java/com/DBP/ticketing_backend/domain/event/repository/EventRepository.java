package com.DBP.ticketing_backend.domain.event.repository;

import com.DBP.ticketing_backend.domain.event.entity.Event;
import com.DBP.ticketing_backend.domain.event.enums.EventStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByStatusAndTicketingStartAtLessThanEqual(
        EventStatus status, LocalDateTime now
    );

    List<Event> findAllByOrderByCreatedAtDesc();

    List<Event> findAllByStatusOrderByCreatedAtDesc(EventStatus status);

    List<Event> findAllByStatusInOrderByCreatedAtDesc(List<EventStatus> statuses);
}
