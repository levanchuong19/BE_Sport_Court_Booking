package com.example.BE_SportCourtBooking.repository;

import com.example.BE_SportCourtBooking.entity.Enum.PriceType;
import com.example.BE_SportCourtBooking.entity.Enum.SlotStatus;
import com.example.BE_SportCourtBooking.entity.Slot;
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

    @Query("SELECT s FROM Slot s WHERE s.bookingStatus = 'PENDING' AND s.createAt <= :timeLimit")
    List<Slot> findOverdueSlots(@Param("timeLimit") Timestamp timeLimit);

    @Query("SELECT c FROM Slot c WHERE " +
            "(:status IS NULL OR c.status = :status) AND " +
            "(:slotType IS NULL OR c.slotType = :slotType)")
    Page<Slot> findByFilters(
            @Param("slotType") PriceType slotType,
            @Param("status") SlotStatus status,
            Pageable pageable);

    @Query("SELECT COUNT(s) > 0 FROM Slot s WHERE s.court.id = :courtId AND s.slotType = :slotType "
            + "AND s.endDate >= :startDate AND s.startDate <= :endDate "
            + "AND s.endTime > :startTime AND s.startTime < :endTime")
    boolean countOverlappingSlots(@Param("courtId") UUID courtId,
                                  @Param("slotType") PriceType slotType,
                                  @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate,
                                  @Param("startTime") String startTime,
                                  @Param("endTime") String endTime);

    @Query("SELECT FUNCTION('DATE', s.startDate) AS bookingDate, COUNT(s) AS total " +
            "FROM Slot s " +
            "WHERE s.bookingStatus = 'PAID' " +
            "AND FUNCTION('YEARWEEK', s.startDate, 1) = FUNCTION('YEARWEEK', CURRENT_DATE, 1) " +
            "GROUP BY FUNCTION('DATE', s.startDate) " +
            "ORDER BY bookingDate")
    List<Object[]> countPaidBookingsPerDayThisWeek();

    @Query(value = "SELECT DATE(s.startDate) AS bookingDate, COUNT(*) AS total " +
            "FROM slot s " +
            "WHERE s.bookingStatus = 'PAID' " +
            "AND YEAR(s.startDate) = YEAR(CURRENT_DATE) " +
            "AND MONTH(s.startDate) = MONTH(CURRENT_DATE) " +
            "GROUP BY DATE(s.startDate) " +
            "ORDER BY bookingDate", nativeQuery = true)
    List<Object[]> countPaidBookingsPerDayThisMonth();


//    @Query("SELECT COALESCE(SUM(s.price), 0) FROM Slot s WHERE s.bookingStatus = 'PAID'")
//    BigDecimal totalRevenueAllCourt();
//
//    @Query("SELECT COALESCE(SUM(s.price), 0) FROM Slot s WHERE s.bookingStatus = 'PAID' AND s.startDate = CURRENT_DATE")
//    BigDecimal revenueTodayAllCourt();
//
//    @Query("SELECT COALESCE(SUM(s.price), 0) FROM Slot s WHERE s.bookingStatus = 'PAID' AND FUNCTION('YEARWEEK', s.startDate, 1) = FUNCTION('YEARWEEK', CURRENT_DATE, 1)")
//    BigDecimal revenueThisWeekAllCourt();

    @Query("SELECT c.courtType, COALESCE(SUM(s.price), 0) " +
            "FROM Slot s JOIN s.court c " +
            "WHERE s.bookingStatus = 'PAID' " +
            "AND FUNCTION('YEAR', s.startDate) = FUNCTION('YEAR', CURRENT_DATE) " +
            "AND FUNCTION('MONTH', s.startDate) = FUNCTION('MONTH', CURRENT_DATE) " +
            "GROUP BY c.courtType " +
            "ORDER BY c.courtType")
    List<Object[]> revenueThisMonthGroupByCourtType();

    @Query("SELECT COUNT(s) FROM Slot s WHERE s.bookingStatus = 'CANCELLED'")
    Long countCancelledBookings();

    @Query("SELECT COUNT(s) FROM Slot s WHERE s.bookingStatus = 'COMPLETED'")
    Long countCompletedBookings();

    @Query("SELECT COUNT(s) FROM Slot s WHERE s.bookingStatus = 'PENDING'")
    Long countPendingBookings();

}
