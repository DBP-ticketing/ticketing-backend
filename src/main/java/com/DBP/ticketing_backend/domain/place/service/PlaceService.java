package com.DBP.ticketing_backend.domain.place.service;

import com.DBP.ticketing_backend.domain.auth.dto.UsersDetails;
import com.DBP.ticketing_backend.domain.place.dto.CreatePlaceRequestDto;
import com.DBP.ticketing_backend.domain.place.dto.PlaceResponseDto;
import com.DBP.ticketing_backend.domain.place.entity.Place;
import com.DBP.ticketing_backend.domain.place.repository.PlaceRepository;
import com.DBP.ticketing_backend.domain.seattemplate.entity.SeatTemplate;
import com.DBP.ticketing_backend.domain.seattemplate.repository.SeatTemplateRepository;
import com.DBP.ticketing_backend.domain.users.entity.Users;
import com.DBP.ticketing_backend.domain.users.enums.UsersRole;
import com.DBP.ticketing_backend.domain.users.repository.UsersRepository;
import com.DBP.ticketing_backend.global.exception.CustomException;
import com.DBP.ticketing_backend.global.exception.ErrorCode;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final SeatTemplateRepository seatTemplateRepository;
    private final UsersRepository usersRepository;

    @Transactional
    public Long createPlace(CreatePlaceRequestDto requestDto, UsersDetails usersDetails) {

        validateAdmin(usersDetails);

        Place place =
                Place.builder()
                        .placeName(requestDto.getPlaceName())
                        .address(requestDto.getAddress())
                        .build();

        Place savedPlace = placeRepository.save(place);

        List<SeatTemplate> seatTemplates = new ArrayList<>();

        for (CreatePlaceRequestDto.CreateSectionDto sectionDto : requestDto.getSections()) {

            String sectionName = sectionDto.getSectionName();
            int rows = sectionDto.getRows();
            int cols = sectionDto.getColumns();

            for (int r = 1; r <= rows; r++) {
                for (int c = 1; c <= cols; c++) {
                    SeatTemplate seatTemplate =
                            SeatTemplate.builder()
                                    .place(savedPlace)
                                    .section(sectionName)
                                    .row(r)
                                    .column(c)
                                    .build();

                    seatTemplates.add(seatTemplate);
                }
            }
        }
        seatTemplateRepository.saveAll(seatTemplates);

        return savedPlace.getPlaceId();
    }

    @Transactional(readOnly = true)
    public PlaceResponseDto getPlace(Long placeId, UsersDetails usersDetails) {

        validateAdmin(usersDetails);

        Place place = findPlaceById(placeId);

        List<SeatTemplate> templates = seatTemplateRepository.findAllByPlace(place);

        return PlaceResponseDto.from(place, templates);
    }

    public void deletePlace(Long placeId, UsersDetails usersDetails) {

        validateAdmin(usersDetails);

        Place place = findPlaceById(placeId);

        seatTemplateRepository.deleteByPlace(place);
        placeRepository.delete(place);
    }

    private void validateAdmin(UsersDetails usersDetails) {
        Users user =
                usersRepository
                        .findById(usersDetails.getUserId())
                        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (user.getRole() != UsersRole.ROLE_ADMIN) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    private Place findPlaceById(Long placeId) {
        return placeRepository
                .findById(placeId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<PlaceResponseDto> getAllPlaces() {
        return placeRepository.findAll().stream()
            .map(place -> PlaceResponseDto.from(place, new ArrayList<>()))
            .collect(Collectors.toList());
    }
}
