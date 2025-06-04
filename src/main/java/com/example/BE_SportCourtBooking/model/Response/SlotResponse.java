package com.example.BE_SportCourtBooking.model.Response;

import com.example.BE_SportCourtBooking.entity.Account;
import com.example.BE_SportCourtBooking.entity.Court;
import com.example.BE_SportCourtBooking.entity.Enum.BookingStatus;
import com.example.BE_SportCourtBooking.entity.Enum.PriceType;
import com.example.BE_SportCourtBooking.entity.Enum.SlotStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SlotResponse {

    Account account;
    Court court;
    LocalDate startDate;
    LocalDate endDate;
    String startTime;
    String endTime;
    PriceType slotType;
    SlotStatus status;
    String paymentUrl;
    BookingStatus bookingStatus ;
    BigDecimal price;
}
