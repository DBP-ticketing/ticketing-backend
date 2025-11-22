package com.DBP.ticketing_backend.domain.place.controller;

import com.DBP.ticketing_backend.domain.auth.dto.UsersDetails;
import com.DBP.ticketing_backend.domain.place.dto.CreatePlaceRequestDto;
import com.DBP.ticketing_backend.domain.place.dto.PlaceResponseDto;
import com.DBP.ticketing_backend.domain.place.service.PlaceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/place")
@RequiredArgsConstructor
@Tag(name = " 장소 api", description = "장소 관리 API 입니다.")
public class PlaceController {

    private final PlaceService placeService;

    @Operation(summary = "장소 생성", description = "장소를 생성합니다.")
    @PostMapping("/create")
    public ResponseEntity<Long> createPlace(
            @RequestBody CreatePlaceRequestDto createPlaceRequestDto,
            @AuthenticationPrincipal UsersDetails usersDetails) {
        return ResponseEntity.ok(placeService.createPlace(createPlaceRequestDto, usersDetails));
    }

    @Operation(summary = "장소 조회", description = "장소 아이디로 장소를 조회합니다.")
    @GetMapping("/{placeId}")
    public ResponseEntity<PlaceResponseDto> getPlace(
            @PathVariable("placeId") Long placeId,
            @AuthenticationPrincipal UsersDetails usersDetails) {
        return ResponseEntity.ok(placeService.getPlace(placeId, usersDetails));
    }

    @Operation(summary = "장소 삭제", description = "장소 아이디로 장소를 삭제합니다.")
    @DeleteMapping("/{placeId}")
    public ResponseEntity<Void> deletePlace(
            @PathVariable("placeId") Long placeId,
            @AuthenticationPrincipal UsersDetails usersDetails) {
        placeService.deletePlace(placeId, usersDetails);
        return ResponseEntity.ok().build();
    }
}
