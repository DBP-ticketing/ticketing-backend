package com.DBP.ticketing_backend.domain.place.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

import java.util.List;

@Data
public class CreatePlaceRequestDto {

    @NotBlank(message = "장소명은 필수입니다.")
    private String placeName;

    @NotBlank(message = "주소는 필수입니다.")
    private String address;

    @NotEmpty(message = "최소 하나 이상의 구역이 필요합니다.")
    private List<CreateSectionDto> sections;

    @Data
    public static class CreateSectionDto {
        @NotBlank(message = "구역명은 필수입니다.")
        private String sectionName; // 예: "A"

        @NotNull
        @Min(value = 1, message = "행은 1 이상이어야 합니다.")
        private Integer rows; // n

        @NotNull
        @Min(value = 1, message = "열은 1 이상이어야 합니다.")
        private Integer columns; // m
    }
}
