package com.example.BE_SportCourtBooking.repository;

import com.example.BE_SportCourtBooking.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Payment findBySlotId(UUID slotId);
}
