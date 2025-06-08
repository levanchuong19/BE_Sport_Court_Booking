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

    public Map<String, Long> getPaidBookingsPerDayThisWeek() {
        try {
            Map<String, Long> result = new LinkedHashMap<>();
            List<Object[]> records = slotRepository.countPaidBookingsPerDayThisWeek();

            for (Object[] record : records) {
                String date = record[0].toString();
                Long total = ((Number) record[1]).longValue();
                result.put(date, total);
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get paid bookings per day this week", e);
        }
    }

    public Map<String, Long> getPaidBookingsPerDayThisMonth() {
        try {
            Map<String, Long> result = new LinkedHashMap<>();
            List<Object[]> records = slotRepository.countPaidBookingsPerDayThisMonth();

            for (Object[] record : records) {
                String date = record[0].toString(); // bookingDate dạng "yyyy-MM-dd"
                Long total = ((Number) record[1]).longValue(); // total số booking
                result.put(date, total);
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get paid bookings per day this month", e);
        }
    }

    public Map<String, BigDecimal> getRevenueThisMonthGroupByCourtType() {
        try {
            Map<String, BigDecimal> result = new LinkedHashMap<>();
            List<Object[]> records = slotRepository.revenueThisMonthGroupByCourtType();

            for (Object[] record : records) {
                String courtType = (String) record[0];
                BigDecimal revenue = (record[1] != null) ? (BigDecimal) record[1] : BigDecimal.ZERO;
                result.put(courtType, revenue);
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get revenue grouped by court type for this month", e);
        }
    }

}
