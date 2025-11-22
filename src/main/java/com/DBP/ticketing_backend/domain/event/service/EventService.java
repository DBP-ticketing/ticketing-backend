package com.DBP.ticketing_backend.domain.event.service;

import com.DBP.ticketing_backend.domain.auth.dto.UsersDetails;
import com.DBP.ticketing_backend.domain.event.dto.CreateEventRequestDto;
import com.DBP.ticketing_backend.domain.event.dto.CreateEventRequestDto.SectionSetting;
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

        if (requestDto.getTicketingStartAt().getMinute() != 0 ||
            requestDto.getTicketingStartAt().getSecond() != 0) {
            // ErrorCode에 "예매 오픈은 정각 단위로만 설정 가능합니다." 추가 필요 (예: ONLY_ON_THE_HOUR)
            throw new CustomException(ErrorCode.ONLY_ON_THE_HOUR);
        }

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

        if (requestDto.getSeatForm() == SeatForm.ASSIGNED) {
            // 지정석: 구역 이름(Key)으로 매핑
            Map<String, CreateEventRequestDto.SectionSetting> settingMap = requestDto.getSeatSettings().stream()
                .collect(Collectors.toMap(CreateEventRequestDto.SectionSetting::getSectionName, s -> s));

            for (SeatTemplate template : templates) {
                CreateEventRequestDto.SectionSetting setting = settingMap.get(template.getSection());
                if (setting == null) throw new CustomException(ErrorCode.SECTION_SETTING_MISSING);

                seatsToSave.add(createSeatEntity(savedEvent, template, setting));
            }
        } else {
            // 스탠딩/자유석: "대표 설정 하나"를 모든 템플릿에 일괄 적용
            // 좌석 레벨과 가격을 통일
            if (requestDto.getSeatSettings().isEmpty()) {
                throw new CustomException(ErrorCode.SECTION_SETTING_MISSING);
            }
            CreateEventRequestDto.SectionSetting commonSetting = requestDto.getSeatSettings().get(0);

            for (SeatTemplate template : templates) {
                // 구역 이름 확인 안 함! 무조건 commonSetting 적용
                seatsToSave.add(createSeatEntity(savedEvent, template, commonSetting));
            }
        }

        seatRepository.saveAll(seatsToSave);
        return savedEvent.getEventId();
    }

    private Seat createSeatEntity(Event event, SeatTemplate template, CreateEventRequestDto.SectionSetting setting) {
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
}
