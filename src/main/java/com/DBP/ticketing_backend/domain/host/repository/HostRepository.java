package com.DBP.ticketing_backend.domain.host.repository;

import com.DBP.ticketing_backend.domain.host.entity.Host;
import com.DBP.ticketing_backend.domain.host.enums.HostStatus;
import com.DBP.ticketing_backend.domain.users.entity.Users;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HostRepository extends JpaRepository<Host, Long> {
    Optional<Host> findByBusinessNumber(String businessNumber);

    List<Host> findByStatus(HostStatus status);

    Optional<Host> findByUsers(Users users);
}
