package com.DBP.ticketing_backend.global.exception;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {

    private final int status;
    private final String message;
}
