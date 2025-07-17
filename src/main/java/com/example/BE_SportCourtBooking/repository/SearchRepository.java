package com.example.BE_SportCourtBooking.repository;

import com.example.BE_SportCourtBooking.entity.BusinessLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SearchRepository extends JpaRepository<BusinessLocation, UUID> {
}
