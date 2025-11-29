package com.DBP.ticketing_backend.domain.event.service;

import com.DBP.ticketing_backend.domain.auth.dto.UsersDetails;
import com.DBP.ticketing_backend.domain.event.dto.CreateEventRequestDto;
import com.DBP.ticketing_backend.domain.event.dto.EventDetailResponseDto;
import com.DBP.ticketing_backend.domain.event.dto.EventListResponseDto;
import com.DBP.ticketing_backend.domain.event.entity.Event;
import com.DBP.ticketing_backend.domain.event.enums.EventStatus;
import com.DBP.ticketing_backend.domain.event.enums.SeatForm;
import com.DBP.ticketing_backend.domain.event.repository.EventRepository;
import com.DBP.ticketing_backend.domain.host.entity.Host;
import com.DBP.ticketing_backend.domain.host.repository.HostRepository;
import com.DBP.ticketing_backend.domain.place.entity.Place;
import com.DBP.ticketing_backend.domain.place.repository.PlaceRepository;
import com.DBP.ticketing_backend.domain.seat.entity.Seat;
import com.DBP.ticketing_backend.domain.seat.enums.SeatStatus;
import com.DBP.ticketing_backend.domain.seat.repository.SeatRepository;
import com.DBP.ticketing_backend.domain.seattemplate.entity.SeatTemplate;
import com.DBP.ticketing_backend.domain.seattemplate.repository.SeatTemplateRepository;
import com.DBP.ticketing_backend.domain.users.entity.Users;
import com.DBP.ticketing_backend.domain.users.repository.UsersRepository;
import com.DBP.ticketing_backend.global.exception.CustomException;
import com.DBP.ticketing_backend.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UsersRepository usersRepository;
    private final HostRepository hostRepository;
    private final PlaceRepository placeRepository;
    private final SeatTemplateRepository seatTemplateRepository;
    private final SeatRepository seatRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public Long createEvent(CreateEventRequestDto requestDto, UsersDetails usersDetails) {

        Users user =
            usersRepository
                .findById(usersDetails.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Host host =
            hostRepository
                .findByUsers(user)
                .orElseThrow(() -> new CustomException(ErrorCode.HOST_NOT_FOUND));

        Place place =
            placeRepository
                .findById(requestDto.getPlaceId())
                .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));

        Event event =
            Event.builder()
                .host(host)
                .place(place)
                .eventName(requestDto.getEventName())
                .category(requestDto.getCategory())
                .date(requestDto.getDate())
                .ticketingStartAt(requestDto.getTicketingStartAt())
                .seatForm(requestDto.getSeatForm())
                .build();

        Event savedEvent = eventRepository.save(event);

        List<SeatTemplate> templates = seatTemplateRepository.findAllByPlace(place);

        if (templates.isEmpty()) {
            throw new CustomException(ErrorCode.TEMPLATE_NOT_FOUND);
        }

        List<Seat> seatsToSave = new ArrayList<>();

        if (requestDto.getSeatSettings().isEmpty()) {
            throw new CustomException(ErrorCode.SECTION_SETTING_MISSING);
        }

        SeatForm seatForm = requestDto.getSeatForm();

        // 1. 자유좌석 (FREE) 또는 스탠딩 (STANDING)의 경우: 단일 가격 적용, 등급 없음
        if (seatForm == SeatForm.FREE || seatForm == SeatForm.STANDING) {

            // 필수: 단 하나의 가격 설정만 허용
            if (requestDto.getSeatSettings().size() != 1) {
                throw new CustomException(ErrorCode.INVALID_REQUEST);
            }

            // 호스트가 입력한 단 하나의 가격 설정을 가져옵니다.
            CreateEventRequestDto.SectionSetting commonSetting = requestDto.getSeatSettings().get(0);

            for (SeatTemplate template : templates) {
                seatsToSave.add(
                    Seat.builder()
                        .event(savedEvent)
                        .template(template)
                        // 등급은 null로 설정 (등급 표시 안 함)
                        .level(null)
                        // 모든 좌석에 동일한 가격을 적용합니다.
                        .price(commonSetting.getPrice()) // <--- 이 부분에서 동일한 가격이 설정됩니다.
                        .status(SeatStatus.AVAILABLE)
                        .build());
            }

        }
        // 2. 지정좌석 (ASSIGNED) 또는 구역별 지정좌석 (SEAT_WITH_SECTION)의 경우: 구역별 등급 및 가격 적용
        else if (seatForm == SeatForm.ASSIGNED || seatForm == SeatForm.SEAT_WITH_SECTION) {

            // 요청받은 구역별 가격 설정을 Map으로 변환하여 찾기 쉽게 준비 (이전 수정 로직)
            Map<String, CreateEventRequestDto.SectionSetting> sectionSettingMap =
                requestDto.getSeatSettings().stream()
                    .collect(
                        Collectors.toMap(
                            CreateEventRequestDto.SectionSetting::getSectionName,
                            setting -> setting
                        )
                    );

            for (SeatTemplate template : templates) {
                String sectionName = template.getSection();

                CreateEventRequestDto.SectionSetting settingForThisSection =
                    sectionSettingMap.get(sectionName);

                if (settingForThisSection == null) {
                    throw new CustomException(ErrorCode.SECTION_SETTING_MISMATCH);
                }

                // 구역별 등급과 가격 적용
                seatsToSave.add(
                    Seat.builder()
                        .event(savedEvent)
                        .template(template)
                        .level(settingForThisSection.getSeatLevel())
                        .price(settingForThisSection.getPrice())
                        .status(SeatStatus.AVAILABLE)
                        .build());
            }
        } else {
            // 정의되지 않은 좌석 형태에 대한 예외 처리
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        seatRepository.saveAll(seatsToSave);
        return savedEvent.getEventId();
    }

    private Seat createSeatEntity(
        Event event, SeatTemplate template, CreateEventRequestDto.SectionSetting setting) {
        return Seat.builder()
            .event(event)
            .template(template)
            .level(setting.getSeatLevel())
            .price(setting.getPrice())
            .status(SeatStatus.AVAILABLE)
            .build();
    }

    @Transactional(readOnly = true)
    public List<EventListResponseDto> getEvents(EventStatus status) {
        List<Event> events;

        if (status == null) {
            events = eventRepository.findAllByOrderByCreatedAtDesc();
        } else {
            events = eventRepository.findAllByStatusOrderByCreatedAtDesc(status);
        }

        return events.stream().map(EventListResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<EventListResponseDto> getMyHostEvents(UsersDetails usersDetails) {
        Users user =
            usersRepository
                .findById(usersDetails.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Host host =
            hostRepository
                .findByUsers(user)
                .orElseThrow(() -> new CustomException(ErrorCode.HOST_NOT_FOUND));

        List<Event> events = eventRepository.findAllByHostOrderByCreatedAtDesc(host);

        return events.stream().map(EventListResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    public EventDetailResponseDto getEvent(Long eventId) {
        Event event =
            eventRepository
                .findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));

        return EventDetailResponseDto.from(event);
    }

    @Transactional
    public void updateEventStatus(Long eventId, EventStatus newStatus) {
        Event event =
            eventRepository
                .findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));

        event.updateStatus(newStatus);

        if (newStatus == EventStatus.CLOSED
            || newStatus == EventStatus.CANCELLED
            || newStatus == EventStatus.COMPLETED) {

            redisTemplate.delete("waiting_queue:" + eventId);
            redisTemplate.delete("active_tokens:" + eventId);
        }
    }

    @Transactional
    public void openEvent(Long eventId) {
        Event event =
            eventRepository
                .findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));

        event.updateStatus(EventStatus.OPEN);
    }

    @Transactional
    public void closeEndedEvent(Long eventId) {
        Event event =
            eventRepository
                .findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));

        processStatusChange(event, EventStatus.CLOSED);
    }

    private void processStatusChange(Event event, EventStatus status) {
        event.updateStatus(status);

        if (status == EventStatus.CLOSED
            || status == EventStatus.CANCELLED
            || status == EventStatus.COMPLETED) {
            String eventIdStr = event.getEventId().toString();
            redisTemplate.delete("waiting_queue:" + eventIdStr);
            redisTemplate.delete("active_tokens:" + eventIdStr);
        }
    }
}