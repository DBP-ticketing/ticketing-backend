package com.DBP.ticketing_backend.domain.bookedseathistory.repository;

import com.DBP.ticketing_backend.domain.bookedseathistory.entity.BookedSeatHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookedSeatHistoryRepository extends JpaRepository<BookedSeatHistory, Long> {

}
