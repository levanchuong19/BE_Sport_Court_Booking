package com.example.BE_SportCourtBooking.model.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackRequest {
    private int overallRating;
    private String comment;
    private int courtQualityRating;
    private int cleanlinessRating;
    private int bookingExperienceRating;
    private LocalDate playedDate;

    private UUID courtId;
    private UUID accountId;
}
