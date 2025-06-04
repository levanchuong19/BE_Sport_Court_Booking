package com.example.BE_SportCourtBooking.model.Response;

import com.example.BE_SportCourtBooking.entity.Account;
import com.example.BE_SportCourtBooking.entity.Enum.CourtStatus;
import com.example.BE_SportCourtBooking.entity.Enum.CourtType;
import com.example.BE_SportCourtBooking.entity.Enum.PriceType;
import com.example.BE_SportCourtBooking.entity.Image;

import com.example.BE_SportCourtBooking.entity.Slot;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CourtResponse {

    CourtType courtType;
    String courtName;
    String location;
    String description;
    CourtStatus status;
    String openTime;
    String closeTime;
    Account courtManager;
    List<CourtResponse.PriceResponse> prices;
    List<Image> images;
    List<Slot> slots;

    @Data
    public static class PriceResponse{
        private PriceType priceType;
        private BigDecimal price;
    }
}
