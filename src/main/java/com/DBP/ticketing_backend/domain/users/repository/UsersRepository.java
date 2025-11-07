package com.DBP.ticketing_backend.domain.users.repository;

import com.DBP.ticketing_backend.domain.users.entity.Users;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UsersRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmail(String email);

}
