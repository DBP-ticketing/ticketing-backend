package com.DBP.ticketing_backend.domain.queue.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QueueResponseDto {
    private Long rank;      // 내 대기 순번
    private boolean active; // 입장 가능 여부
}
