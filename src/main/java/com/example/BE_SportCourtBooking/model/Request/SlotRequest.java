package com.example.BE_SportCourtBooking.model.Request;
import com.example.BE_SportCourtBooking.entity.Enum.PaymentMethod;
import com.example.BE_SportCourtBooking.entity.Enum.PriceType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class SlotRequest {
    @NotNull(message = "Slot type is required!")
    @Enumerated(EnumType.STRING)
    @Column(name = "slot_type", nullable = false)
    PriceType slotType;

    @NotNull(message = "Account ID cannot be null!")
    @Column(nullable = false)
    UUID account;

    @NotNull(message = "Court ID cannot be null!")
    @Column(nullable = false)
    UUID court;

    @NotNull
    LocalDate startDate;

    @NotNull
    LocalDate endDate;

    @NotNull(message = "Start time cannot be null!")
    @Column(nullable = false)
    @Pattern(regexp = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$",
            message = "Start time must be in HH:mm or HH:mm:ss format!")
    String startTime;

    @NotNull(message = "End time cannot be null!")
    @Column(nullable = false)
    @Pattern(regexp = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$",
            message = "End time must be in HH:mm or HH:mm:ss format!")
    String endTime;

    @NotNull(message = "Payment method cannot be null!")
    @Column(nullable = false)
    PaymentMethod paymentMethod;

}
