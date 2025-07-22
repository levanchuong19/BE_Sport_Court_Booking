package com.example.BE_SportCourtBooking.api;

import com.example.BE_SportCourtBooking.model.Response.*;
import com.example.BE_SportCourtBooking.service.StatisticAdminService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/stats")
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@PreAuthorize("hasRole('ADMIN')")
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

    @GetMapping("/booking-this-year")
    public ResponseEntity<Map<String, Long>> getBookingThisYear() {
        Map<String, Long> counts = statisticAdminService.getPaidBookingsPerMonthThisYear();

        return ResponseEntity.ok(counts);
    }

//    @GetMapping("/revenue-this-month-eachCourt")
//    public ResponseEntity<Map<String, BigDecimal>> getRevenueThisMonthCourtType() {
//        Map<String, BigDecimal> sum = statisticAdminService.getRevenueThisMonthGroupByCourtType();
//
//        return ResponseEntity.ok(sum);
//    }

    @GetMapping("/booking-revenue-today")
    public ResponseEntity<Map<String, Object>> getBookingsAndRevenueToday() {
        Map<String, Object> list = statisticAdminService.getTodayPaidBookingAndRevenue();

        return ResponseEntity.ok(list);
    }

    @GetMapping("/customer-yesterday-today")
    public ResponseEntity<Map<String, Long>> getCustomersYesterdayToday() {
        Map<String, Long> list = statisticAdminService.getCustomerAccountTodayYesterday();

        return ResponseEntity.ok(list);
    }

    @GetMapping("/new-manager-this-year")
    public ResponseEntity<List<Map<String, Object>>> getNewManagersThisYear() {
        List<Map<String, Object>> list = statisticAdminService.getNewManagersPerMonthThisYear();

        return ResponseEntity.ok(list);
    }

//    @GetMapping("/all-manager-summary")
//    public ResponseEntity<Page<Map<String, Object>>> getAllManagersSummary(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size) {
//
//        Pageable pageable = PageRequest.of(page, size);
//
//        Page<Map<String, Object>> result = statisticAdminService.getPaidBookingsAndRevenueWithManagerInfo(pageable);
//
//        return ResponseEntity.ok(result);
//    }
//
//    @GetMapping("/all-court-summary")
//    public ResponseEntity<Page<Map<String, Object>>> getAllCourtsSummary(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size) {
//
//        Pageable pageable = PageRequest.of(page, size);
//        Page<Map<String, Object>> pageResult = statisticAdminService.getAllCourtsWithManagerAndPaidStats(pageable);
//
//        return ResponseEntity.ok(pageResult);
//    }


    @GetMapping("/top5-court-bookings")
    public ResponseEntity<List<Map<String, Object>>> getTop5CourtsBookings() {
        List<Map<String, Object>> list = statisticAdminService.getTop5CourtsByPaidBookings();

        return ResponseEntity.ok(list);
    }
}
