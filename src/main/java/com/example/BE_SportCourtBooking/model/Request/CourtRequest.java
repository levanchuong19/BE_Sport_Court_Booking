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

    @NotBlank(message = "Description cannot be blank!")
    @Column(name = "description", nullable = false)
    String description;


    @NotNull(message = "Court must have a manager account!")
    UUID manager_id;

    @NotNull(message = "Please provide at least one price!")
    List<PriceRequest> prices;

    List<String> images;

    @NotNull(message = "Business Location Id is required!")
    UUID businessLocationId;

    @NotNull(message = "Construction year cannot be null!")
    @Column(name = "construction_year")
    Integer yearBuild;

    @NotNull(message = "Length cannot be null!")
    @Column(name = "length")
    Double length;

    @NotNull(message = "Width cannot be null!")
    @Column(name = "width")
    Double width;

    @NotNull(message = "Maximum number of players cannot be null!")
    @Column(name = "max_players")
    Integer maxPlayers;

    @Data
    public static class PriceRequest {
        @NotNull(message = "Price type is required!")
        private PriceType priceType;

        @NotNull(message = "Price cannot be null!")
        private BigDecimal price;
    }

}
