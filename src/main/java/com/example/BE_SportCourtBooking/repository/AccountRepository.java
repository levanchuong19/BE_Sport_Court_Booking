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
    Account findAccountById(UUID id);

    Account findAccountByPhone(String phone);

    Optional<Account> findAccountByEmail(String email);

    Account findAccountByRole(Role role);

    @Query("SELECT COUNT(acc) FROM Account acc")
    Long countTotalAccounts();

    @Query("SELECT COUNT(acc) FROM Account acc WHERE acc.role = :role AND acc.isDelete = false")
    Long countAccountsByRole(@Param("role") Role role);

    @Query(value = """
    SELECT COUNT(*) FROM accounts
    WHERE created_at >= CURDATE()
      AND created_at < CURDATE() + INTERVAL 1 DAY
      AND is_deleted = false
    """, nativeQuery = true)
    Long countNewAccountsToday();

    @Query(value = """
    SELECT COUNT(*) FROM accounts
    WHERE created_at >= STR_TO_DATE(CONCAT(YEAR(CURDATE()), WEEK(CURDATE(), 1), ' Monday'), '%X%V %W')
      AND created_at < STR_TO_DATE(CONCAT(YEAR(CURDATE()), WEEK(CURDATE(), 1) + 1, ' Monday'), '%X%V %W')
      AND is_deleted = false
    """, nativeQuery = true)
    Long countNewAccountsThisWeek();

    @Query(value = """
    SELECT COUNT(*) FROM accounts
    WHERE created_at >= DATE_FORMAT(CURDATE() ,'%Y-%m-01')
      AND created_at < DATE_FORMAT(CURDATE() + INTERVAL 1 MONTH, '%Y-%m-01')
      AND is_deleted = false
    """, nativeQuery = true)
    Long countNewAccountsThisMonth();

    @Query("SELECT COUNT(a) FROM Account a WHERE a.isDelete = false")
    Long countActiveAccounts();

    @Query("SELECT COUNT(a) FROM Account a WHERE a.isDelete = true")
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

    @Query(value = "SELECT a.id AS manager_id, a.full_name, a.phone, " +
            "COUNT(s.id) AS total_bookings, COALESCE(SUM(s.price), 0) AS total_revenue " +
            "FROM slots s " +
            "JOIN courts c ON s.court_id = c.id " +
            "JOIN accounts a ON c.manager_id = a.id " +
            "WHERE s.booking_status = 'PAID' " +
            "AND a.role = 'MANAGER' " +
            "GROUP BY a.id, a.full_name, a.phone " +
            "ORDER BY a.id \n-- #pageable\n",
            countQuery = "SELECT COUNT(DISTINCT a.id) " +
                    "FROM slots s " +
                    "JOIN courts c ON s.court_id = c.id " +
                    "JOIN accounts a ON c.manager_id = a.id " +
                    "WHERE s.booking_status = 'PAID' " +
                    "AND a.role = 'MANAGER'",
            nativeQuery = true)
    Page<Object[]> countTotalPaidBookingsAndRevenueWithManagerBasicInfo(Pageable pageable);



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
