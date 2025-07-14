package com.example.BE_SportCourtBooking.model.Response;

import com.example.BE_SportCourtBooking.entity.Account;
import com.example.BE_SportCourtBooking.entity.Court;
import com.example.BE_SportCourtBooking.entity.Enum.BookingStatus;
import com.example.BE_SportCourtBooking.entity.Enum.PaymentMethod;
import com.example.BE_SportCourtBooking.entity.Enum.PriceType;
import com.example.BE_SportCourtBooking.entity.Enum.SlotStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class SlotResponse {

    UUID id;
    Account account;
    CourtResponse court;
    LocalDate startDate;
    LocalDate endDate;
    String startTime;
    String endTime;
    PriceType slotType;
    SlotStatus status;
    BookingStatus bookingStatus ;
    BigDecimal price;
    Boolean reminderSent;
    PaymentMethod paymentMethod;
}
