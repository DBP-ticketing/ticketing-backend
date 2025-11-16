package com.DBP.ticketing_backend.domain.auth.dto.response;

import com.DBP.ticketing_backend.domain.host.entity.Host;
import com.DBP.ticketing_backend.domain.host.enums.HostStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HostResponseDto {

    private Long hostId;
    private String email;
    private String name;
    private String phoneNumber;
    private String companyName;
    private String businessNumber;
    private HostStatus status;

    // Entity → DTO 변환
    public static HostResponseDto from(Host host) {
        return HostResponseDto.builder()
                .hostId(host.getHostId())
                .email(host.getUsers().getEmail())
                .name(host.getUsers().getName())
                .phoneNumber(host.getUsers().getPhoneNumber())
                .companyName(host.getCompanyName())
                .businessNumber(host.getBusinessNumber())
                .status(host.getStatus())
                .build();
    }
}
