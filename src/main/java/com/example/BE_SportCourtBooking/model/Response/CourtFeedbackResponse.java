package com.example.BE_SportCourtBooking.model.Response;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CourtFeedbackResponse {
    private UUID id;

    private int overallRating;
    private String comment;

    private int courtQualityRating;
    private int cleanlinessRating;
    private int bookingExperienceRating;

    private LocalDate playedDate;
    private boolean anonymous;

    // Thông tin người đánh giá (ẩn nếu anonymous)
    private String fullName;
    private String avatar;
}
