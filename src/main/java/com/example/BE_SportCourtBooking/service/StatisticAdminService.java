package com.example.BE_SportCourtBooking.service;

import com.example.BE_SportCourtBooking.entity.Enum.CourtStatus;
import com.example.BE_SportCourtBooking.entity.Enum.CourtType;
import com.example.BE_SportCourtBooking.entity.Enum.Role;
import com.example.BE_SportCourtBooking.model.Response.*;
import com.example.BE_SportCourtBooking.repository.AccountRepository;
import com.example.BE_SportCourtBooking.repository.CourtRepository;
import com.example.BE_SportCourtBooking.repository.SlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatisticAdminService {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    CourtRepository courtRepository;

    @Autowired
    SlotRepository slotRepository;

    public List<AccountRoleCountResponse> getCountAccountByRole() {
        List<AccountRoleCountResponse> list = new ArrayList<>();

        try {
            list.add(new AccountRoleCountResponse("TOTAL", accountRepository.countTotalAccounts()));
            list.add(new AccountRoleCountResponse(Role.CUSTOMER.name(), accountRepository.countAccountsByRole(Role.CUSTOMER)));
            list.add(new AccountRoleCountResponse(Role.STAFF.name(), accountRepository.countAccountsByRole(Role.STAFF)));
            list.add(new AccountRoleCountResponse(Role.MANAGER.name(), accountRepository.countAccountsByRole(Role.MANAGER)));
        } catch (Exception e) {
            throw new RuntimeException("Failed to get account counts", e);
        }

        return list;
    }

    public List<AccountCountByDateResponse> getCountAccountByDate() {
        List<AccountCountByDateResponse> list = new ArrayList<>();

        try {
            list.add(new AccountCountByDateResponse("countToday", accountRepository.countNewAccountsToday()));
            list.add(new AccountCountByDateResponse("countWeek", accountRepository.countNewAccountsThisWeek()));
            list.add(new AccountCountByDateResponse("countMonth", accountRepository.countNewAccountsThisMonth()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to get account counts", e);
        }

        return list;
    }

    public List<AccountCountByDisableResponse> getCountAccountByDisable() {
        List<AccountCountByDisableResponse> list = new ArrayList<>();

        try {
            list.add(new AccountCountByDisableResponse(false, accountRepository.countActiveAccounts()));
            list.add(new AccountCountByDisableResponse(true, accountRepository.countDeletedAccounts()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to get account counts", e);
        }
        return list;
    }

    public List<CourtStatusCountResponse> getCountCourtGroupByStatus() {
        try {
            Long totalCount = courtRepository.countAllCourts();
            List<Object[]> counts = courtRepository.countGroupByStatus();

            return counts.stream()
                    .map(row -> new CourtStatusCountResponse(
                            (CourtStatus) row[0],
                            (Long) row[1],
                            totalCount
                    ))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Failed to get court counts grouped by status", e);
        }
    }

    public List<CourtTypeCountResponse> getCountCourtGroupByType() {
        try {
            List<Object[]> counts = courtRepository.countGroupByType();

            return counts.stream()
                    .map(row -> new CourtTypeCountResponse(
                            (CourtType) row[0],
                            (Long) row[1]
                    ))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Failed to get court counts grouped by type", e);
        }
    }

    public Map<String, Long> getPaidBookingCountsSummary() {
        try {
            Map<String, Long> result = new LinkedHashMap<>();

            result.put("total", slotRepository.countTotalPaidBookings());
            result.put("today", slotRepository.countPaidBookingsToday());
            result.put("thisWeek", slotRepository.countPaidBookingsThisWeek());
            result.put("thisMonth", slotRepository.countPaidBookingsThisMonth());

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get paid booking counts summary", e);
        }
    }

    public Map<String, BigDecimal> getRevenueSummaryAllCourts() {
        try {
            Map<String, BigDecimal> result = new LinkedHashMap<>();

            result.put("total", slotRepository.totalRevenueAllCourt());
            result.put("today", slotRepository.revenueTodayAllCourt());
            result.put("thisWeek", slotRepository.revenueThisWeekAllCourt());
            result.put("thisMonth", slotRepository.revenueThisMonthAllCourt());

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get revenue summary", e);
        }
    }

    public Map<String, Long> getBookingStatusCounts() {
        try {
            Map<String, Long> result = new LinkedHashMap<>();

            result.put("cancelled", slotRepository.countCancelledBookings());
            result.put("completed", slotRepository.countCompletedBookings());
            result.put("pending", slotRepository.countPendingBookings());

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get booking status counts", e);
        }
    }

}
