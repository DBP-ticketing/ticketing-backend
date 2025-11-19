package com.DBP.ticketing_backend.domain.seattemplate.repository;

import com.DBP.ticketing_backend.domain.place.entity.Place;
import com.DBP.ticketing_backend.domain.seattemplate.entity.SeatTemplate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatTemplateRepository extends JpaRepository<SeatTemplate, Long> {

    List<SeatTemplate> findAllByPlace(Place place);

    void deleteByPlace(Place place);
}
