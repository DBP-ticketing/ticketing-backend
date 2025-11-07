package com.DBP.ticketing_backend.domain.auth.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignUpHostRequestDto {

    private String email;
    private String password;
    private String name;
    private String phoneNumber;
    private String companyName;
    private String businessNumber;


}
