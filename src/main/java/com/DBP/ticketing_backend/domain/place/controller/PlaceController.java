package com.DBP.ticketing_backend.domain.place.controller;

import com.DBP.ticketing_backend.domain.auth.dto.UsersDetails;
import com.DBP.ticketing_backend.domain.place.dto.CreatePlaceRequestDto;
import com.DBP.ticketing_backend.domain.place.dto.PlaceResponseDto;
import com.DBP.ticketing_backend.domain.place.service.PlaceService;
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
public class PlaceController {

    private final PlaceService placeService;

    @PostMapping("/create")
    public ResponseEntity<Long> createPlace(
        @RequestBody CreatePlaceRequestDto createPlaceRequestDto,
        @AuthenticationPrincipal UsersDetails usersDetails) {
        return ResponseEntity.ok(placeService.createPlace(createPlaceRequestDto, usersDetails));
    }

    @GetMapping("/{placeId}")
    public ResponseEntity<PlaceResponseDto> getPlace(
        @PathVariable("placeId") Long placeId,
        @AuthenticationPrincipal UsersDetails usersDetails) {
        return ResponseEntity.ok(placeService.getPlace(placeId, usersDetails));
    }

    @DeleteMapping("/{placeId}")
    public ResponseEntity<Void> deletePlace(
        @PathVariable("placeId") Long placeId,
        @AuthenticationPrincipal UsersDetails usersDetails) {
        placeService.deletePlace(placeId, usersDetails);
        return ResponseEntity.ok().build();
    }
}
