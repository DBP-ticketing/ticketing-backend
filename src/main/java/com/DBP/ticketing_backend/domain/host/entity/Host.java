package com.DBP.ticketing_backend.domain.host.entity;

import com.DBP.ticketing_backend.domain.host.enums.HostStatus;
import com.DBP.ticketing_backend.domain.users.entity.Users;
import com.DBP.ticketing_backend.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Host extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "host_id")
    private Long hostId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private Users users;

    @Column(nullable = false, length = 100)
    private String companyName;

    @Column(nullable = false, unique = true, length = 20)
    private String businessNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HostStatus status;

    @Builder
    public Host(Users users, String companyName, String businessNumber, HostStatus status) {
        this.users = users;
        this.companyName = companyName;
        this.businessNumber = businessNumber;
        this.status = status != null ? status : HostStatus.PENDING;
    }

    public void approve() {
        this.status = HostStatus.ACTIVE;
    }

    public void reject() {
        this.status = HostStatus.REJECTED;
    }

    public void suspend() {
        this.status = HostStatus.SUSPENDED;
    }

    public boolean isActive() {
        return this.status == HostStatus.ACTIVE;
    }

    public boolean isPending() {
        return this.status == HostStatus.PENDING;
    }

    public boolean isRejected() {
        return this.status == HostStatus.REJECTED;
    }
}
