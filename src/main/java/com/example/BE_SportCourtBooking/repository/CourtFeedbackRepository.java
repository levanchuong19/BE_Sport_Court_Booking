package com.example.BE_SportCourtBooking.repository;

import com.example.BE_SportCourtBooking.entity.CourtFeedback;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CourtFeedbackRepository extends JpaRepository<CourtFeedback, UUID> {
    List<CourtFeedback> findByCourt_Id(UUID courtId);
    List<CourtFeedback> findByAccount_Id(UUID accountId);
    @EntityGraph(attributePaths = {"account"})
    List<CourtFeedback> findAll(); // Sẽ fetch account cùng lúc
}
