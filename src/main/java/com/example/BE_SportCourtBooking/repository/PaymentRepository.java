package com.example.BE_SportCourtBooking.repository;

import com.example.BE_SportCourtBooking.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findBySlotId(UUID slotId);

}
