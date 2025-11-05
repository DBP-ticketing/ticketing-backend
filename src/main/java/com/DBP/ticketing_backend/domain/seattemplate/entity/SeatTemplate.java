package com.DBP.ticketing_backend.domain.seattemplate.entity;

import com.DBP.ticketing_backend.domain.place.entity.Place;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeatTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(nullable = false, length = 10)
    private String section;

    @Column(nullable = false)
    private Integer row;
    @Column(nullable = false)
    private Integer column;

    @Builder
    public SeatTemplate(Place place, String section, Integer row, Integer column) {
        this.place = place;
        this.section = section;
        this.row = row;
        this.column = column;
    }
}
