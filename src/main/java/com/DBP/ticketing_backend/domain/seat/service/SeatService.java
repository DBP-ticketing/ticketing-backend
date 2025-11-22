package com.DBP.ticketing_backend.domain.seat.service;

import com.DBP.ticketing_backend.domain.seat.dto.SeatResponseDto;
import com.DBP.ticketing_backend.domain.seat.entity.Seat;
import com.DBP.ticketing_backend.domain.seat.repository.SeatRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;

    @Transactional(readOnly = true)
    public List<SeatResponseDto> getSeatsByEvent(Long eventId) {
        // 1. 해당 이벤트의 모든 좌석 조회
        List<Seat> seats = seatRepository.findAllByEventId(eventId);

        // 2. DTO 변환
        return seats.stream()
            .map(SeatResponseDto::from)
            .collect(Collectors.toList());
    }

}
