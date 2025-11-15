package com.DBP.ticketing_backend.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto<T> {

    private boolean success;
    private String message;
    private T data;

    // 성공 응답 (데이터 있음)
    public static <T> AuthResponseDto<T> success(String message, T data) {
        return AuthResponseDto.<T>builder().success(true).message(message).data(data).build();
    }

    // 성공 응답 (데이터 없음)
    public static <T> AuthResponseDto<T> success(String message) {
        return AuthResponseDto.<T>builder().success(true).message(message).build();
    }

    // 실패 응답
    public static <T> AuthResponseDto<T> error(String message) {
        return AuthResponseDto.<T>builder().success(false).message(message).build();
    }
}
