package com.DBP.ticketing_backend.domain.event.scheduler;

import com.DBP.ticketing_backend.domain.event.entity.Event;
import com.DBP.ticketing_backend.domain.event.enums.EventStatus;
import com.DBP.ticketing_backend.domain.event.repository.EventRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventScheduler {

    private final EventRepository eventRepository;

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void openTicketingEvents() {
        LocalDateTime now = LocalDateTime.now();

        List<Event> eventsToOpen =
                eventRepository.findByStatusAndTicketingStartAtLessThanEqual(
                        EventStatus.SCHEDULED, now);

        if (eventsToOpen.isEmpty()) {
            return;
        }

        for (Event event : eventsToOpen) {
            event.updateStatus(EventStatus.OPEN);
            log.info(
                    "Event ID {} is now OPEN! (Scheduled Time: {})",
                    event.getEventId(),
                    event.getTicketingStartAt());
        }
    }
}
