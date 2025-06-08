package com.example.BE_SportCourtBooking.repository;
import com.example.BE_SportCourtBooking.entity.Court;
import com.example.BE_SportCourtBooking.entity.Enum.CourtStatus;
import com.example.BE_SportCourtBooking.entity.Enum.CourtType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CourtRepository extends JpaRepository<Court, UUID> {
    Court findCourtById(UUID id);
    @Query("SELECT c FROM Court c WHERE " +
            "(:courtType IS NULL OR c.courtType = :courtType) AND " +
            "(:status IS NULL OR c.status = :status) AND " +
            "(:isDelete IS NULL OR c.isDelete = :isDelete) AND " +
            "(:courtName IS NULL OR c.courtName LIKE %:courtName%)")
    Page<Court> findByFilters(
            @Param("courtType") CourtType courtType,
            @Param("status") CourtStatus status,
            @Param("courtName") String courtName,
            @Param("isDelete") Boolean isDelete,
            Pageable pageable);
    @Query("SELECT c FROM Court c WHERE c.businessLocation.id = :businessLocationId " +
            "AND (:isDelete IS NULL OR c.isDelete = :isDelete)")
    Page<Court> findCourtsByBusinessLocationId(@Param("businessLocationId") UUID businessLocationId,
                                                                  @Param("isDelete") Boolean isDelete,
                                                                  Pageable pageable);

}
