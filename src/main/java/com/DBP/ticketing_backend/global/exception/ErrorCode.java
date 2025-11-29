package com.DBP.ticketing_backend.global.exception;

import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 400 Bad Request: 잘못된 요청
    NOT_A_PENDING_HOST(HttpStatus.BAD_REQUEST, "대기중인 호스트가 아닙니다."),
    NOT_AN_ACTIVATED_HOST(HttpStatus.BAD_REQUEST, "활성화된 호스트가 아닙니다."),
    INVALID_EMAIL_OR_PASSWORD(HttpStatus.BAD_REQUEST, "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_TICKET_OPEN_TIME(HttpStatus.BAD_REQUEST, "예매 오픈 시간은 공연 시간보다 빨라야 합니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    SOLD_OUT(HttpStatus.BAD_REQUEST, "매진된 공연입니다."),
    ALREADY_RESERVED(HttpStatus.BAD_REQUEST, "이미 예약된 좌석입니다."),
    INVALID_BOOKING_STATUS(HttpStatus.BAD_REQUEST, "유효하지 않은 예약 상태입니다."),
    TICKETING_NOT_OPEN(HttpStatus.BAD_REQUEST, "예매가 오픈되지 않은 공연입니다."),
    TICKETING_CLOSED(HttpStatus.BAD_REQUEST, "예매가 종료된 공연입니다."),
    ONLY_ON_THE_HOUR(HttpStatus.BAD_REQUEST, "예매 오픈은 정각 단위로만 설정 가능합니다."),
    // 401 Unauthorized: 인증되지 않은 사용자
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요한 요청입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "권한이 없는 접근입니다."),
    INVALID_SEAT_STATUS(HttpStatus.UNAUTHORIZED, "유효하지 않은 좌석 상태입니다."),
    INVALID_QUEUE_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 대기열 토큰입니다."),
    QUEUE_TOKEN_EXPIRED(HttpStatus.FORBIDDEN, "대기열 입장 권한이 만료되었거나 없습니다. 다시 줄을 서주세요."),
    // 404 Not Found: 리소스를 찾을 수 없음
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다."),
    HOST_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 호스트를 찾을 수 없습니다."),
    TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 토큰을 찾을 수 없습니다."),
    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 장소를 찾을 수 없습니다."),
    TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 좌석 템플릿을 찾을 수 없습니다."),
    SECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 섹션을 찾을 수 없습니다."),
    SECTION_SETTING_MISSING(HttpStatus.NOT_FOUND, "섹션 설정이 누락되었습니다."),
    EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 공연을 찾을 수 없습니다."),
    SEAT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 좌석을 찾을 수 없습니다."),
    BOOKING_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 예약을 찾을 수 없습니다."),
    SECTION_SETTING_MISMATCH(HttpStatus.NOT_FOUND, "모든 좌석 템플릿 구역에 대한 가격 설정이 누락되었습니다."),

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
