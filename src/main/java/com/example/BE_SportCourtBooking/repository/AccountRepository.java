package com.example.BE_SportCourtBooking.repository;

import com.example.BE_SportCourtBooking.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    Account findAccountById(UUID id);
    Account findAccountByPhone(String phone);
    Account findAccountByFullName(String fullName);
}
