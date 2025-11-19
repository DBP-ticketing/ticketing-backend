package com.DBP.ticketing_backend.domain.place.dto;

import com.DBP.ticketing_backend.domain.place.entity.Place;
import com.DBP.ticketing_backend.domain.seattemplate.entity.SeatTemplate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Builder
public class PlaceResponseDto {

    private Long placeId;
    private String placeName;
    private String address;
    private List<SectionInfo> sections;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SectionInfo {
        private String sectionName; // 구역명 (예: "A")
        private Integer rowCount; // 행 개수 (n)
        private Integer colCount; // 열 개수 (m)
    }

    public static PlaceResponseDto from(Place place, List<SeatTemplate> templates) {

        Map<String, List<SeatTemplate>> groupedBySection =
                templates.stream().collect(Collectors.groupingBy(SeatTemplate::getSection));

        List<SectionInfo> sectionInfos =
                groupedBySection.entrySet().stream()
                        .map(
                                entry -> {
                                    String sectionName = entry.getKey();
                                    List<SeatTemplate> seats = entry.getValue();

                                    int maxRow =
                                            seats.stream()
                                                    .mapToInt(SeatTemplate::getRow)
                                                    .max()
                                                    .orElse(0);

                                    int maxCol =
                                            seats.stream()
                                                    .mapToInt(SeatTemplate::getColumn)
                                                    .max()
                                                    .orElse(0);

                                    return SectionInfo.builder()
                                            .sectionName(sectionName)
                                            .rowCount(maxRow)
                                            .colCount(maxCol)
                                            .build();
                                })
                        .sorted(Comparator.comparing(SectionInfo::getSectionName))
                        .collect(Collectors.toList());

        return PlaceResponseDto.builder()
                .placeId(place.getPlaceId())
                .placeName(place.getPlaceName())
                .address(place.getAddress())
                .sections(sectionInfos)
                .build();
    }
}
