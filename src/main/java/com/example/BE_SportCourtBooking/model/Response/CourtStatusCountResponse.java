package com.example.BE_SportCourtBooking.model.Response;

import com.example.BE_SportCourtBooking.entity.Enum.CourtStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourtStatusCountResponse {
    private CourtStatus status;
    private Long count;
    private Long countTotal;
}
