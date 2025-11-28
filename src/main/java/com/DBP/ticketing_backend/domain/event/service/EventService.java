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

        // [수정: 예매 시작 시간 정각 단위 제한 제거]
        /*
        if (requestDto.getTicketingStartAt().getMinute() != 0
                || requestDto.getTicketingStartAt().getSecond() != 0) {
            // ErrorCode에 "예매 오픈은 정각 단위로만 설정 가능합니다." 추가 필요 (예: ONLY_ON_THE_HOUR)
            throw new CustomException(ErrorCode.ONLY_ON_THE_HOUR);
        }
        */

        Event event =
            Event.builder()
                .host(host)
                .place(place)
                .eventName(requestDto.getEventName())
                .category(requestDto.getCategory())
                .date(requestDto.getDate())
                .ticketingStartAt(requestDto.getTicketingStartAt())
                .seatForm(requestDto.getSeatForm())
                .build(); // status는 디폴트(SCHEDULED) 적용

        Event savedEvent = eventRepository.save(event);

        // 해당 장소의 모든 좌석 템플릿 가져오기
        List<SeatTemplate> templates = seatTemplateRepository.findAllByPlace(place);

        if (templates.isEmpty()) {
            throw new CustomException(ErrorCode.TEMPLATE_NOT_FOUND); // "좌석 템플릿이 존재하지 않습니다."
        }

        List<Seat> seatsToSave = new ArrayList<>();

        // [수정된 로직]
        // 현재 프론트엔드가 단일 좌석 설정만 보내므로, SeatForm(지정석/자유석/스탠딩)에 관계없이
        // 이 단일 설정(price, level)을 모든 좌석 템플릿에 적용하도록 로직을 간소화합니다.

        if (requestDto.getSeatSettings().isEmpty()) {
            // 좌석을 생성하려면 최소한 가격과 등급에 대한 정보(SectionSetting)가 하나 필요합니다.
            throw new CustomException(ErrorCode.SECTION_SETTING_MISSING);
        }

        // 프론트에서 넘어온 첫 번째 설정을 공통 설정으로 사용
        CreateEventRequestDto.SectionSetting commonSetting = requestDto.getSeatSettings().get(0);

        for (SeatTemplate template : templates) {
            seatsToSave.add(createSeatEntity(savedEvent, template, commonSetting));
        }
        // [기존의 if(ASSIGNED) / else 로직은 위 코드로 대체되어 삭제됩니다.]


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
            // 파라미터가 없으면 전체 조회
            events = eventRepository.findAllByOrderByCreatedAtDesc();

        } else {
            // 파라미터가 있으면 해당 상태만 조회
            events = eventRepository.findAllByStatusOrderByCreatedAtDesc(status);
        }

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

        // 1. DB 상태 변경
        event.updateStatus(newStatus);

        // 2. 예매 불가능 상태가 되면 Redis 대기열 데이터 삭제 (메모리 확보)
        if (newStatus == EventStatus.CLOSED
            || newStatus == EventStatus.CANCELLED
            || newStatus == EventStatus.COMPLETED) {

            // 대기열(Waiting) 삭제
            redisTemplate.delete("waiting_queue:" + eventId);

            // 입장객(Active) 삭제
            // (선택사항: 이미 들어온 사람은 결제하게 둘 건지, 강제로 쫓아낼 건지 정책에 따라 결정)
            // 보통 취소/종료면 쫓아내는 게 맞고, 마감(CLOSED)이면 들어온 사람은 결제하게 둬도 됨.
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

        // 공통 로직 호출 (CLOSED로 변경)
        processStatusChange(event, EventStatus.CLOSED);
    }

    private void processStatusChange(Event event, EventStatus status) {
        event.updateStatus(status);

        // 종료/취소/마감 상태라면 대기열 삭제
        if (status == EventStatus.CLOSED
            || status == EventStatus.CANCELLED
            || status == EventStatus.COMPLETED) {
            String eventIdStr = event.getEventId().toString();
            redisTemplate.delete("waiting_queue:" + eventIdStr);
            redisTemplate.delete("active_tokens:" + eventIdStr);
        }
    }
}