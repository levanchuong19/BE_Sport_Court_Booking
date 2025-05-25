package com.example.BE_SportCourtBooking.repository;
import com.example.BE_SportCourtBooking.entity.Court;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CourtRepository extends JpaRepository<Court, UUID> {
    Court findCourtById(UUID id);
//    Court findCourtByName(String name);
//    Court findCourtByAddress(String address);
}
