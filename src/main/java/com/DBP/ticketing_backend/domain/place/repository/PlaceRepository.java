package com.DBP.ticketing_backend.domain.place.repository;

import com.DBP.ticketing_backend.domain.place.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceRepository extends JpaRepository<Place, Long> {

}
