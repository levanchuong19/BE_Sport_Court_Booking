package com.example.BE_SportCourtBooking.repository;

import com.example.BE_SportCourtBooking.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {
    List<Report> findReportByStaffId (UUID staffId);
    List<Report> findReportByBusinessLocationId (UUID businessLocationId);

}
