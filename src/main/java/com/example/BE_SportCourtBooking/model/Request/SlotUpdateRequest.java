package com.example.BE_SportCourtBooking.model.Request;

import com.example.BE_SportCourtBooking.entity.Enum.PriceType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
@Data
public class SlotUpdateRequest {
    @NotNull(message = "Slot type is required!")
    @Enumerated(EnumType.STRING)
    @Column(name = "slot_type", nullable = false)
    PriceType slotType;

    @Column(nullable = false)
    LocalDate date;

    @NotBlank(message = "Open time cannot be blank!")
    @Pattern(regexp = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$",
            message = "Start time must be in HH:mm or HH:mm:ss format!")
    @Column(nullable = false)
    String startTime;

    @NotBlank(message = "Close time cannot be blank!")
    @Pattern(regexp = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$",
            message = "End time must be in HH:mm or HH:mm:ss format!")
    @Column(nullable = false)
    String endTime;

//    SlotRequest.PriceRequest prices;
//
//    @Data
//    public static class PriceRequest {
//        @NotNull(message = "Price type is required!")
//        private PriceType priceType;
//
//        @NotNull(message = "Price cannot be null!")
//        private BigDecimal price;
//    }
}
