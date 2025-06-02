package com.example.BE_SportCourtBooking.repository;

import com.example.BE_SportCourtBooking.entity.Enum.PriceType;
import com.example.BE_SportCourtBooking.entity.Enum.SlotStatus;
import com.example.BE_SportCourtBooking.entity.Slot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public interface SlotRepository extends JpaRepository<Slot, UUID> {
    Slot findSlotById(UUID id);
//    @Query("SELECT COUNT(s) > 0 FROM Slot s WHERE s.court.id = :courtId " +
//            "AND s.date = :date " +
//            "AND ((s.startTime < :endTime AND s.endTime > :startTime))")
//    boolean existsOverlappingSlot(
//            @Param("courtId") UUID courtId,
//            @Param("date") LocalDate date,
//            @Param("startTime") LocalTime startTime,
//            @Param("endTime") LocalTime endTime);
//
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



}
