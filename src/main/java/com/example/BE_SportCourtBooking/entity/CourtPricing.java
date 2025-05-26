package com.example.BE_SportCourtBooking.entity;

import com.example.BE_SportCourtBooking.entity.Enum.PriceType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "court_pricing")
public class CourtPricing {
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        @Column(name = "id", updatable = false, nullable = false)
        UUID id;

        @NotNull(message = "Court is required!")
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "court_id", nullable = false)
        @JsonIgnore
        Court court;

        @NotNull(message = "Price type is required!")
        @Enumerated(EnumType.STRING)
        @Column(name = "price_type", nullable = false)
        PriceType priceType; // Enum: HOURLY, DAILY, WEEKLY, MONTHLY

        @NotNull(message = "Price cannot be null!")
        @Positive(message = "Price must be positive!")
        @Column(name = "price", nullable = false)
        BigDecimal price;
}

