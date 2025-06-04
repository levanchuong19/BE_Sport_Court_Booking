package com.example.BE_SportCourtBooking.repository;

import com.example.BE_SportCourtBooking.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
}
