package com.example.BE_SportCourtBooking.model.Response;

import com.example.BE_SportCourtBooking.entity.Account;
import com.example.BE_SportCourtBooking.entity.BusinessLocation;
import com.example.BE_SportCourtBooking.entity.Enum.CourtStatus;
import com.example.BE_SportCourtBooking.entity.Enum.CourtType;
import com.example.BE_SportCourtBooking.entity.Enum.PriceType;
import com.example.BE_SportCourtBooking.entity.Image;

import com.example.BE_SportCourtBooking.entity.Slot;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class CourtResponse {

    UUID id;
    CourtType courtType;
    String courtName;
    String description;
    CourtStatus status;
    List<CourtResponse.PriceResponse> prices;
    List<Image> images;
    Integer yearBuild;
    Double length;
    Double width;
    Integer maxPlayers;

    @Data
    public static class PriceResponse{
        private PriceType priceType;
        private BigDecimal price;
    }
}
