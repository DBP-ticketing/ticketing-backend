package com.DBP.ticketing_backend.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {
    private String email;
    private String name;
    private String role;
    private String accessToken;
    private String refreshToken;
}
