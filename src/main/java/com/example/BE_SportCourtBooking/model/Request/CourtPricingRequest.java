package com.example.BE_SportCourtBooking.model.Request;

import com.example.BE_SportCourtBooking.entity.Enum.PriceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourtPricingRequest {
    @NotNull(message = "Price type is required!")
    PriceType priceType;

    @NotNull(message = "Price cannot be null!")
    @Positive(message = "Price must be positive!")
    BigDecimal price;
}
