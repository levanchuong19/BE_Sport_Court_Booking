package com.example.BE_SportCourtBooking.model.Request;

import com.example.BE_SportCourtBooking.entity.Enum.CourtStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourtStatusRequest {
    @NotNull(message = "Court status is required!")
    private CourtStatus status;
}
