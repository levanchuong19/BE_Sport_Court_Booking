package com.example.BE_SportCourtBooking.repository;

import com.example.BE_SportCourtBooking.entity.Support;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SupportRepository extends JpaRepository<Support, UUID> {
}
