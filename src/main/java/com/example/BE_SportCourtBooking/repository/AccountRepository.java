package com.example.BE_SportCourtBooking.repository;

import com.example.BE_SportCourtBooking.entity.Account;
import com.example.BE_SportCourtBooking.entity.Enum.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    Account findAccountById(UUID id);

    Account findAccountByPhone(String phone);

    Account findAccountByEmail(String fullName);

    Account findAccountByRole(Role role);

    @Query("SELECT COUNT(acc) FROM Account acc")
    Long countTotalAccounts();

    @Query("SELECT COUNT(acc) FROM Account acc WHERE acc.role = :role AND acc.isDelete = false")
    Long countAccountsByRole(@Param("role") Role role);

    @Query("SELECT COUNT(a) FROM Account a WHERE DATE(a.createAt) = CURRENT_DATE AND a.isDelete = false")
    Long countNewAccountsToday();

    @Query(value = "SELECT COUNT(*) FROM account WHERE YEAR(create_at) = YEAR(CURDATE()) AND WEEK(create_at, 1) = WEEK(CURDATE(), 1) AND is_deleted = false", nativeQuery = true)
    Long countNewAccountsThisWeek();

    @Query(value = "SELECT COUNT(*) FROM account WHERE YEAR(create_at) = YEAR(CURDATE()) AND MONTH(create_at) = MONTH(CURDATE()) AND is_deleted = false", nativeQuery = true)
    Long countNewAccountsThisMonth();

    @Query("SELECT COUNT(a) FROM Account a WHERE a.isDelete = false")
    Long countActiveAccounts();

    @Query("SELECT COUNT(a) FROM Account a WHERE a.isDelete = true")
    Long countDeletedAccounts();
}
