package com.DBP.ticketing_backend.domain.auth.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignUpUserRequestDto {

    private String email;
    private String password;
    private String name;
    private String phoneNumber;
}
