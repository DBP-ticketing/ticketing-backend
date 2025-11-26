package com.DBP.ticketing_backend.domain.event.scheduler;

import com.DBP.ticketing_backend.domain.event.entity.Event;
import com.DBP.ticketing_backend.domain.event.enums.EventStatus;
import com.DBP.ticketing_backend.domain.event.repository.EventRepository;

import com.DBP.ticketing_backend.domain.event.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.RedisTemplate;
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
    private final EventService eventService;
    private final RedisTemplate<String, String> redisTemplate;
    @Scheduled(cron = "0 * * * * *")
    public void openTicketingEvents() {
        LocalDateTime now = LocalDateTime.now();

        List<Event> eventsToOpen = eventRepository.findByStatusAndTicketingStartAtLessThanEqual(
            EventStatus.SCHEDULED, now);

        if (eventsToOpen.isEmpty()) {
            return;
        }

        for (Event event : eventsToOpen) {
            try {
                eventService.openEvent(event.getEventId());

                log.info("Event ID {} is now OPEN!", event.getEventId());
            } catch (Exception e) {
                log.error("Event {} 오픈 처리 실패", event.getEventId(), e);
            }
        }
    }

    @Scheduled(cron = "0 * * * * *")
    public void closeEndedEvents() {
        LocalDateTime now = LocalDateTime.now();
        List<Event> endedEvents = eventRepository.findByStatusAndDateBefore(EventStatus.OPEN, now);

        for (Event event : endedEvents) {
            try {
                // 서비스에게 "이거 닫아줘"라고 위임
                eventService.closeEndedEvent(event.getEventId());
            } catch (Exception e) {
                log.error("Event {} 자동 종료 실패", event.getEventId(), e);
            }
        }
    }
}
