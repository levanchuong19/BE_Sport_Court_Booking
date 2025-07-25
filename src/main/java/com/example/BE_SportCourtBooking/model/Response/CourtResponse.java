package com.example.BE_SportCourtBooking.model.Response;
import com.example.BE_SportCourtBooking.entity.BusinessLocation;
import com.example.BE_SportCourtBooking.entity.Enum.CourtStatus;
import com.example.BE_SportCourtBooking.entity.Enum.CourtType;
import com.example.BE_SportCourtBooking.entity.Enum.PriceType;
import com.example.BE_SportCourtBooking.entity.Image;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
    Boolean isDelete;
    BusinessLocation businessLocation;

    @Data
    public static class PriceResponse{
        private PriceType priceType;
        private BigDecimal price;
    }



}
