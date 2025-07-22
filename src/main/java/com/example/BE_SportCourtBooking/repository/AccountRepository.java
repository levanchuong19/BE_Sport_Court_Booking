package com.example.BE_SportCourtBooking.repository;

import com.example.BE_SportCourtBooking.entity.Account;
import com.example.BE_SportCourtBooking.entity.Enum.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    Account findAccountById(UUID id);

    Account findAccountByPhone(String phone);

    Optional<Account> findAccountByEmail(String email);

    Account findAccountByRole(Role role);

    @Query("SELECT COUNT(acc) FROM Account acc")
    Long countTotalAccounts();

    @Query("SELECT COUNT(acc) FROM Account acc WHERE acc.role = :role AND acc.isDelete = false")
    Long countAccountsByRole(@Param("role") Role role);

    @Query("SELECT COUNT(a) FROM Account a WHERE DATE(a.createAt) = CURRENT_DATE AND a.isDelete = false")
    Long countNewAccountsToday();

    @Query("SELECT COUNT(a) FROM Account a WHERE FUNCTION('YEARWEEK', a.createAt, 1) = FUNCTION('YEARWEEK', CURRENT_DATE, 1) AND a.isDelete = false")
    Long countNewAccountsThisWeek();

    @Query("SELECT COUNT(a) FROM Account a WHERE FUNCTION('YEAR', a.createAt) = FUNCTION('YEAR', CURRENT_DATE) AND FUNCTION('MONTH', a.createAt) = FUNCTION('MONTH', CURRENT_DATE) AND a.isDelete = false")
    Long countNewAccountsThisMonth();

    @Query("SELECT COUNT(a) FROM Account a WHERE a.isDelete = false")
    Long countActiveAccounts();

    @Query(value = "SELECT COUNT(a) FROM Account a WHERE a.isDelete = true")
    Long countDeletedAccounts();

    @Query("SELECT COUNT(a) FROM Account a " +
            "WHERE a.role = 'CUSTOMER' " +
            "AND DATE(a.createAt) = CURRENT_DATE")
    Long countTodayCustomerAccounts();

    @Query("SELECT COUNT(a) FROM Account a " +
            "WHERE a.role = 'CUSTOMER' " +
            "AND a.createAt >= :startDate " +
            "AND a.createAt < :endDate")
    Long countYesterdayCustomerAccounts(@Param("startDate") Date startDate, @Param("endDate") Date endDate);


    @Query(value = "SELECT MONTH(a.created_at) AS month, COUNT(*) AS total " +
            "FROM accounts a " +
            "WHERE a.role = 'MANAGER' " +
            "AND YEAR(a.created_at) = YEAR(CURRENT_DATE) " +
            "GROUP BY MONTH(a.created_at) " +
            "ORDER BY month", nativeQuery = true)
    List<Object[]> countNewManagersPerMonthThisYear();

    @Query(
            value = "SELECT " +
                    "    a.id AS managerId, " +
                    "    a.full_name AS fullName, " +
                    "    a.phone AS phone, " +
                    "    COUNT(DISTINCT CASE WHEN p.status = 'COMPLETED' THEN s.id END) AS totalBookings, " +
                    "    COALESCE(SUM(CASE WHEN p.status = 'COMPLETED' THEN p.amount ELSE 0 END), 0) AS totalRevenue " +
                    "FROM " +
                    "    accounts a " +
                    "LEFT JOIN " +
                    "    courts c ON a.id = c.manager_id " +
                    "LEFT JOIN " +
                    "    slots s ON c.id = s.court_id " +
                    "LEFT JOIN " +
                    "    payments p ON s.id = p.slot_id " +
                    "WHERE " +
                    "    a.role = 'MANAGER' " +
                    "GROUP BY " +
                    "    a.id, a.full_name, a.phone " +
                    "ORDER BY " +
                    "    totalRevenue DESC",

            countQuery = "SELECT COUNT(*) FROM accounts a WHERE a.role = 'MANAGER'", // <-- countQuery tùy chỉnh

            nativeQuery = true
    )
    Page<Object[]> getManagerStatistics(Pageable pageable);


    @Query(value = "SELECT c.id, c.court_name, a.full_name, " +
            "COUNT(s.id), COALESCE(SUM(s.price), 0) " +
            "FROM courts c " +
            "JOIN accounts a ON c.manager_id = a.id " +
            "LEFT JOIN slots s ON s.court_id = c.id AND s.booking_status = 'PAID' " +
            "GROUP BY c.id, c.court_name, a.full_name " +
            "ORDER BY c.court_name",
            countQuery = "SELECT COUNT(DISTINCT c.id) FROM courts c " +
                    "JOIN accounts a ON c.manager_id = a.id",
            nativeQuery = true)
    Page<Object[]> findAllCourtsWithManagerNameAndPaidBookingStats(Pageable pageable);


}
