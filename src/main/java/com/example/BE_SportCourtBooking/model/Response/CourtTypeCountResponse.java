package com.example.BE_SportCourtBooking.model.Response;

import com.example.BE_SportCourtBooking.entity.Enum.CourtType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourtTypeCountResponse {
    private CourtType type;
    private Long count;
}
