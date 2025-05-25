package com.example.BE_SportCourtBooking.model.Request;
import com.example.BE_SportCourtBooking.entity.Enum.CourtType;
import com.example.BE_SportCourtBooking.entity.Enum.PriceType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class CourtRequest {

    @NotNull(message = "Court type is required!")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    CourtType courtType;

    @NotNull(message = "Court name cannot be null!")
    @NotBlank(message = "Court name cannot be blank!")
    @Column(name = "fullName", nullable = false)
    String courtName;

    @NotBlank(message = "Location cannot be blank!")
    @Column(name = "location", nullable = false)
    String location;

    @NotBlank(message = "Description cannot be blank!")
    @Column(name = "description", nullable = false)
    String description;

    @NotBlank(message = "Open time cannot be blank!")
    @Pattern(regexp = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$",
            message = "Open time must be in HH:mm or HH:mm:ss format!")
    String openTime;

    @NotBlank(message = "Close time cannot be blank!")
    @Pattern(regexp = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$",
            message = "Close time must be in HH:mm or HH:mm:ss format!")
    String closeTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    @NotNull(message = "Court must have a manager account!")
    UUID manager_id;

    List<PriceRequest> prices;

    List<String> images;

    @Data
    public static class PriceRequest {
        @NotNull(message = "Price type is required!")
        private PriceType priceType;

        @NotNull(message = "Price cannot be null!")
        private BigDecimal price;
    }

}
