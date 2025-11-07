package com.DBP.ticketing_backend.domain.host.repository;

import com.DBP.ticketing_backend.domain.host.entity.Host;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HostRepository extends JpaRepository<Host, Long> {
    Optional<Host> findByBusinessNumber(String businessNumber);
}
