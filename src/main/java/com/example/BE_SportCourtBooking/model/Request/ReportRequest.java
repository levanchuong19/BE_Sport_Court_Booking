package com.example.BE_SportCourtBooking.model.Request;

import com.example.BE_SportCourtBooking.entity.BusinessLocation;
import com.example.BE_SportCourtBooking.entity.Court;
import com.example.BE_SportCourtBooking.entity.Report;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {

    @NotNull(message = "Business Location cannot be null!")
    @Column(name = "business_location_id", nullable = false)
    private UUID businessLocation;

    @NotNull(message = "Court cannot be null!")
    @Column(name = "court_id", nullable = false)
    private UUID court;

    @NotNull(message = "Content cannot be null!")
    @Column(columnDefinition = "TEXT")
    private String content;

    @NotNull(message = "Recipient email cannot be null!")
    @Column (name = "recipient_email", nullable = false)
    private String recipientEmail;

}
