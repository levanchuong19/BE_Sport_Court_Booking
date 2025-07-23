package com.example.BE_SportCourtBooking.repository;

import com.example.BE_SportCourtBooking.entity.Enum.PriceType;
import com.example.BE_SportCourtBooking.entity.Enum.SlotStatus;
import com.example.BE_SportCourtBooking.entity.Slot;
import com.example.BE_SportCourtBooking.model.Response.BookingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SlotRepository extends JpaRepository<Slot, UUID> {
    Slot findSlotById(UUID id);

    List<Slot> findSlotByAccountId(UUID accountId);

//    @Query("""
//    SELECT new com.example.BE_SportCourtBooking.model.Response.BookingResponse(
//        s.id, s.startDate, s.endDate, s.startTime, s.endTime,
//        s.slotType, s.status, s.bookingStatus,
//        new com.example.BE_SportCourtBooking.model.Response.CourtResponse(c.id, c.courtType,c.courtName, c.description,c.status, null, null, c.yearBuild, c.length, c.width, c.maxPlayers, c.businessLocation)
//    )
//    FROM Slot s
//    JOIN s.court c
//    WHERE s.account.id = :accountId
//""")
//    List<BookingResponse> findBookingResponsesByAccountId(@Param("accountId") UUID accountId);

    @Query("""
        SELECT new com.example.BE_SportCourtBooking.model.Response.BookingResponse(
            s.id, s.startDate, s.endDate, s.startTime, s.endTime,
            s.slotType, s.status, s.bookingStatus,
            new com.example.BE_SportCourtBooking.model.Response.CourtResponse(
                c.id, c.courtType, c.courtName, c.description, c.status, null, null,
                c.yearBuild, c.length, c.width, c.maxPlayers, c.businessLocation
            ),
            p.amount
        )
        FROM Slot s
        JOIN s.court c
        LEFT JOIN Payment p WITH p.slot = s
        WHERE s.account.id = :accountId
    """)
    List<BookingResponse> findBookingResponsesByAccountId(@Param("accountId") UUID accountId);

    @Query("SELECT s FROM Slot s WHERE s.bookingStatus = 'PENDING' AND s.createAt <= :timeLimit")
    List<Slot> findOverdueSlots(@Param("timeLimit") Timestamp timeLimit);

//    @Query("SELECT c FROM Slot c WHERE " +
//            "(:status IS NULL OR c.status = :status) AND " +
//            "(:isDelete IS NULL OR c.isDelete = :isDelete) AND " +
//            "(:slotType IS NULL OR c.slotType = :slotType)")
//    Page<Slot> findByFilters(
//            @Param("slotType") PriceType slotType,
//            @Param("status") SlotStatus status,
//            @Param("isDelete") Boolean isDelete,
//            Pageable pageable);

    @Query("SELECT s FROM Slot s " +
            "LEFT JOIN FETCH s.account a " +
            "LEFT JOIN FETCH s.court c " +
            "LEFT JOIN FETCH s.payment p " +
            "WHERE (:status IS NULL OR s.status = :status) AND " +
            "(:isDelete IS NULL OR s.isDelete = :isDelete) AND " +
            "(:slotType IS NULL OR s.slotType = :slotType)")
    Page<Slot> findByFilters(
            @Param("slotType") PriceType slotType,
            @Param("status") SlotStatus status,
            @Param("isDelete") Boolean isDelete,
            Pageable pageable);

    @Query("SELECT COUNT(s) FROM Slot s WHERE s.court.id = :courtId AND s.slotType = :slotType " +
            "AND s.bookingStatus <> 'OVERDUE' " +
            "AND s.endDate >= :startDate AND s.startDate <= :endDate " +
            "AND s.endTime > :startTime AND s.startTime < :endTime")
    long countOverlappingSlots(@Param("courtId") UUID courtId,
                               @Param("slotType") PriceType slotType,
                               @Param("startDate") LocalDate startDate,
                               @Param("endDate") LocalDate endDate,
                               @Param("startTime") String startTime,
                               @Param("endTime") String endTime);

    @Query(value = "SELECT COUNT(*) FROM Slot s WHERE s.status = 'COMPLETED' AND DATE(s.create_at) = CURDATE()", nativeQuery = true)
    Long countTodayPaidBookings();

    @Query(
            value = "SELECT COALESCE(SUM(p.amount), 0) " +
                    "FROM slots s " +
                    "JOIN payments p ON p.slot_id = s.id " +
                    "WHERE p.status = 'COMPLETED' " +
                    "AND DATE(s.create_at) = CURDATE()",
            nativeQuery = true
    )
    BigDecimal sumTodayPaidIncome();

    @Query("SELECT FUNCTION('DATE', s.startDate) AS bookingDate, COUNT(s) AS total " +
            "FROM Slot s " +
            "WHERE s.bookingStatus = 'PAID' " +
            "AND FUNCTION('YEARWEEK', s.startDate, 1) = FUNCTION('YEARWEEK', CURRENT_DATE, 1) " +
            "GROUP BY FUNCTION('DATE', s.startDate) " +
            "ORDER BY bookingDate")
    List<Object[]> countPaidBookingsPerDayThisWeek();

    @Query(value = "SELECT MONTH(s.start_date) AS month, COUNT(*) AS total " +
            "FROM slots s " +
            "WHERE s.booking_status = 'PAID' " +
            "AND YEAR(s.start_date) = YEAR(CURRENT_DATE) " +
            "GROUP BY MONTH(s.start_date) " +
            "ORDER BY month", nativeQuery = true)
    List<Object[]> countPaidBookingsPerMonthThisYear();

//    @Query("SELECT COALESCE(SUM(s.price), 0) FROM Slot s WHERE s.bookingStatus = 'PAID'")
//    BigDecimal totalRevenueAllCourt();
//
//    @Query("SELECT COALESCE(SUM(s.price), 0) FROM Slot s WHERE s.bookingStatus = 'PAID' AND s.startDate = CURRENT_DATE")
//    BigDecimal revenueTodayAllCourt();
//
//    @Query("SELECT COALESCE(SUM(s.price), 0) FROM Slot s WHERE s.bookingStatus = 'PAID' AND FUNCTION('YEARWEEK', s.startDate, 1) = FUNCTION('YEARWEEK', CURRENT_DATE, 1)")
//    BigDecimal revenueThisWeekAllCourt();

    @Query(value = "SELECT " +
            "    c.type, " +
            "    COALESCE(SUM(p.amount), 0) AS total_revenue " +
            "FROM " +
            "    courts c " +
            "JOIN " +
            "    slots s ON c.id = s.court_id " +
            "JOIN " +
            "    payments p ON s.id = p.slot_id " +
            "WHERE " +
            "    p.status = 'COMPLETED' " +
            "    AND YEAR(p.payment_date) = YEAR(CURRENT_DATE) " +
            "    AND MONTH(p.payment_date) = MONTH(CURRENT_DATE) " +
            "GROUP BY " +
            "    c.type " +
            "ORDER BY " +
            "    total_revenue DESC",
            nativeQuery = true)
    List<Object[]> revenueThisMonthGroupByCourtType();

    @Query("SELECT COUNT(s) FROM Slot s WHERE s.bookingStatus = 'CANCELLED'")
    Long countCancelledBookings();

    @Query("SELECT COUNT(s) FROM Slot s WHERE s.bookingStatus = 'COMPLETED'")
    Long countCompletedBookings();

    @Query("SELECT COUNT(s) FROM Slot s WHERE s.bookingStatus = 'PENDING'")
    Long countPendingBookings();

    @Query(value = "SELECT * FROM booking WHERE STR_TO_DATE(start_time, '%H:%i:%s') BETWEEN :start AND :end", nativeQuery = true)
    List<Slot> findAllByStartTimeBetween(
            @Param("start") String start,
            @Param("end") String end
    );

    @Query("SELECT s FROM Slot s WHERE s.status = :status " +
            "AND s.isDelete = false " +
            "AND s.bookingStatus != 'COMPLETED' " + // Loại bỏ slot đã hoàn tất
            "AND (s.endDate < :currentTime OR (s.endDate = :currentTime AND s.endTime <= :currentTimeStr))")
    List<Slot> findFinishedSlots(
            @Param("status") SlotStatus status,
            @Param("currentTime") LocalDate currentTime,
            @Param("currentTimeStr") String currentTimeStr
    );

    @Query("SELECT s FROM Slot s WHERE s.status = :status " +
            "AND s.isDelete = false " +
            "AND (s.startDate = :currentDate AND s.startTime <= :currentTimeStr)")
    List<Slot> findCheckedInSlots(
            @Param("status") SlotStatus status,
            @Param("currentDate") LocalDate currentDate,
            @Param("currentTimeStr") String currentTimeStr
    );
}
