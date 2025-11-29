package com.DBP.ticketing_backend.domain.seattemplate.repository;

import com.DBP.ticketing_backend.domain.place.entity.Place;
import com.DBP.ticketing_backend.domain.seattemplate.entity.SeatTemplate;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SeatTemplateRepository extends JpaRepository<SeatTemplate, Long> {

    List<SeatTemplate> findAllByPlace(Place place);

    void deleteByPlace(Place place);

    @Query("SELECT DISTINCT st.section FROM SeatTemplate st WHERE st.place.placeId = :placeId")
    List<String> findDistinctSectionNamesByPlaceId(@Param("placeId") Long placeId);
}
