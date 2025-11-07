package com.DBP.ticketing_backend.domain.place.entity;

import com.DBP.ticketing_backend.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Place extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_id")
    private Long placeId;

    @Column(nullable = false, length = 100)
    private String placeName;

    @Column(nullable = false, length = 200)
    private String address;

    @Builder
    public Place(String placeName, String address) {
        this.placeName = placeName;
        this.address = address;
    }
}
