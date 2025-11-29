package com.DBP.ticketing_backend.domain.place.controller;

import com.DBP.ticketing_backend.domain.auth.dto.UsersDetails;
import com.DBP.ticketing_backend.domain.place.dto.CreatePlaceRequestDto;
import com.DBP.ticketing_backend.domain.place.dto.PlaceResponseDto;
import com.DBP.ticketing_backend.domain.place.service.PlaceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @Operation(
            summary = "장소 생성",
            description =
                    """
            새로운 장소를 생성합니다.

            **권한:**
            - 관리자 권한이 필요합니다.

            **참고:**
            - 요청 본문에 장소 정보를 포함해야 합니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "장소 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    @PostMapping("/create")
    public ResponseEntity<Long> createPlace(
            @RequestBody CreatePlaceRequestDto createPlaceRequestDto,
            @AuthenticationPrincipal UsersDetails usersDetails) {
        return ResponseEntity.ok(placeService.createPlace(createPlaceRequestDto, usersDetails));
    }

    @Operation(
            summary = "장소 조회",
            description =
                    """
            장소 ID를 사용하여 특정 장소의 정보를 조회합니다.

            **권한:**
            - 관리자 권한이 필요합니다.

            **참고:**
            - 유효하지 않은 장소 ID를 전달하면 404 에러가 반환됩니다.
            """)
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = @Content(schema = @Schema(implementation = PlaceResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "장소를 찾을 수 없음"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    @GetMapping("/{placeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    public ResponseEntity<PlaceResponseDto> getPlace(
            @PathVariable("placeId") Long placeId,
            @AuthenticationPrincipal UsersDetails usersDetails) {
        return ResponseEntity.ok(placeService.getPlace(placeId, usersDetails));
    }

    @Operation(
            summary = "장소 삭제",
            description =
                    """
            장소 ID를 사용하여 특정 장소를 삭제합니다.

            **권한:**
            - 관리자 권한이 필요합니다.

            **참고:**
            - 유효하지 않은 장소 ID를 전달하면 404 에러가 반환됩니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "장소 삭제 성공"),
        @ApiResponse(responseCode = "404", description = "장소를 찾을 수 없음"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    @DeleteMapping("/{placeId}")
    public ResponseEntity<Void> deletePlace(
            @PathVariable("placeId") Long placeId,
            @AuthenticationPrincipal UsersDetails usersDetails) {
        placeService.deletePlace(placeId, usersDetails);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "장소 전체 목록 조회", description = "등록된 모든 장소의 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<PlaceResponseDto>> getAllPlaces() {
        return ResponseEntity.ok(placeService.getAllPlaces());
    }

    @Operation(
        summary = "장소의 구역(Section) 목록 조회",
        description = "특정 장소 ID를 사용하여 해당 장소에 등록된 모든 고유한 좌석 구역명(section name) 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = String.class))), // List of Strings
        @ApiResponse(responseCode = "404", description = "장소를 찾을 수 없음"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    @GetMapping("/{placeId}/sections")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    public ResponseEntity<List<String>> getPlaceSections(@PathVariable("placeId") Long placeId) {
        return ResponseEntity.ok(placeService.getPlaceSections(placeId));
    }
}
