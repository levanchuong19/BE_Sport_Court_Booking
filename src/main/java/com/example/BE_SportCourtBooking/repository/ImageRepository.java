package com.example.BE_SportCourtBooking.repository;

import com.example.BE_SportCourtBooking.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ImageRepository extends JpaRepository<Image, UUID> {
    // Additional query methods can be defined here if needed
}
