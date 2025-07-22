package com.example.BE_SportCourtBooking.model.Response;

import com.example.BE_SportCourtBooking.entity.Enum.PriceType;
import com.example.BE_SportCourtBooking.entity.Enum.SlotStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
    public class SlotDTO {
        private UUID id;
        private String accountUsername; // Username từ Account
        private String courtName; // Tên sân từ Court
        private PriceType slotType;
        private SlotStatus status;
        private LocalDate startDate;
        private LocalDate endDate;
        private String startTime;
        private String endTime;
        private BigDecimal price; // Giá tiền từ Payment
    }

