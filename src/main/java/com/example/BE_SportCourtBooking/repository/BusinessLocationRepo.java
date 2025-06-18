package com.example.BE_SportCourtBooking.repository;

import com.example.BE_SportCourtBooking.entity.BusinessLocation;
import com.example.BE_SportCourtBooking.entity.Court;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BusinessLocationRepo extends JpaRepository<BusinessLocation, UUID> {
@Query("SELECT bl FROM BusinessLocation bl " +
        "LEFT JOIN FETCH bl.courts c " +
        "WHERE (:name IS NULL OR bl.name LIKE %:name%) " +
        "AND (:address IS NULL OR bl.address LIKE %:address%) " +
        "AND (:isDelete IS NULL OR bl.isDelete = :isDelete) ")
Page<BusinessLocation> findByFilters(@Param("name") String name,
                                     @Param("address") String address,
                                     @Param("isDelete") Boolean isDelete,
                                     Pageable pageable);
    @Query("SELECT bl FROM BusinessLocation bl LEFT JOIN FETCH bl.courts c WHERE bl.id = :id AND bl.isDelete = false AND (c.isDelete = false OR c.isDelete IS NULL)")
    BusinessLocation findBusinessLocationById(@Param("id") UUID id);
    @Query("SELECT c FROM BusinessLocation c WHERE c.owner.id = :ownerId " +
            "AND (:isDelete IS NULL OR c.isDelete = :isDelete)")
    Page<BusinessLocation> findBusinessLocationsByOwnerId(@Param("ownerId") UUID ownerId,
                                               @Param("isDelete") Boolean isDelete,
                                               Pageable pageable);


    @Query("SELECT bl, COUNT(s.id) as bookingCount " +
            "FROM BusinessLocation bl " +
            "JOIN bl.courts c " +
            "LEFT JOIN Slot s ON s.court.id = c.id " +
            "GROUP BY bl " +
            "ORDER BY bookingCount DESC")
    List<Object[]> findTop3BusinessLocationsByBookingCount(Pageable pageable);
}
