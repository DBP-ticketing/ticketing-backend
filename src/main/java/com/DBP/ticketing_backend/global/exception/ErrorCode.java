package com.DBP.ticketing_backend.global.exception;

import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 400 Bad Request: 잘못된 요청
    NOT_A_PENDING_HOST(HttpStatus.BAD_REQUEST, "대기중인 호스트가 아닙니다."),
    NOT_AN_ACTIVATED_HOST(HttpStatus.BAD_REQUEST, "활성화된 호스트가 아닙니다."),
    INVALID_EMAIL_OR_PASSWORD(HttpStatus.BAD_REQUEST, "이메일 또는 비밀번호가 올바르지 않습니다."),

    // 401 Unauthorized: 인증되지 않은 사용자
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요한 요청입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "권한이 없는 접근입니다."),

    // 404 Not Found: 리소스를 찾을 수 없음
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다."),
    HOST_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 호스트를 찾을 수 없습니다."),
    TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 토큰을 찾을 수 없습니다."),

    // 409 Conflict: 충돌
    DUPLICATE_MEMBER_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    DUPLICATE_BUSINESS_NUMBER(HttpStatus.CONFLICT, "이미 등록된 사업자번호입니다."),

    // 500 Internal Server Error: 서버 내부 오류
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부에 알 수 없는 오류가 발생했습니다.");

    private final HttpStatus status; // HTTP 상태 코드
    private final String message; // 에러 메시지

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
