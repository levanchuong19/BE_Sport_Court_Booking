package com.example.BE_SportCourtBooking.api;

import com.example.BE_SportCourtBooking.model.Response.*;
import com.example.BE_SportCourtBooking.service.StatisticAdminService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/admin/stats")
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class StatisticAdminAPI {
    @Autowired
    StatisticAdminService statisticAdminService;

    @GetMapping("/accounts-by-role")
    public ResponseEntity<List<AccountRoleCountResponse>> getAllAccountsByRole() {
        List<AccountRoleCountResponse> list = statisticAdminService.getCountAccountByRole();

        return ResponseEntity.ok(list);
    }

    @GetMapping("/accounts-by-date")
    public ResponseEntity<List<AccountCountByDateResponse>> getAllAccountsByDate() {
        List<AccountCountByDateResponse> list = statisticAdminService.getCountAccountByDate();

        return ResponseEntity.ok(list);
    }

    @GetMapping("/account-active-status")
    public ResponseEntity<List<AccountCountByDisableResponse>> getAllAccountsByActiveStatus() {
        List<AccountCountByDisableResponse> list = statisticAdminService.getCountAccountByDisable();

        return ResponseEntity.ok(list);
    }

    @GetMapping("/court-status")
    public ResponseEntity<List<CourtStatusCountResponse>> getAllCourtsByStatus() {
        List<CourtStatusCountResponse> list = statisticAdminService.getCountCourtGroupByStatus();

        return ResponseEntity.ok(list);
    }

    @GetMapping("/court-type")
    public ResponseEntity<List<CourtTypeCountResponse>> getAllCourtsByType() {
        List<CourtTypeCountResponse> list = statisticAdminService.getCountCourtGroupByType();

        return ResponseEntity.ok(list);
    }

    @GetMapping("/booking-status-counts")
    public ResponseEntity<Map<String, Long>> getBookingStatusCounts() {
        Map<String, Long> counts = statisticAdminService.getBookingStatusCounts();
        return ResponseEntity.ok(counts);
    }

    @GetMapping("/booking-this-week")
    public ResponseEntity<Map<String, Long>> getBookingThisWeek() {
        Map<String, Long> counts = statisticAdminService.getPaidBookingsPerDayThisWeek();

        return ResponseEntity.ok(counts);
    }

    @GetMapping("/booking-this-month")
    public ResponseEntity<Map<String, Long>> getBookingThisMonth() {
        Map<String, Long> counts = statisticAdminService.getPaidBookingsPerDayThisMonth();

        return ResponseEntity.ok(counts);
    }

    @GetMapping("/revenue-this-month-eachCourt")
    public ResponseEntity<Map<String, BigDecimal>> getRevenueThisMonthCourtType() {
        Map<String, BigDecimal> sum = statisticAdminService.getRevenueThisMonthGroupByCourtType();

        return ResponseEntity.ok(sum);
    }
}
