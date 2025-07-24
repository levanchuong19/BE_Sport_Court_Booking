package com.example.BE_SportCourtBooking.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "court_feedbacks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourtFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private float overallRating; // tổng điểm 1-5

    @Column(columnDefinition = "TEXT")
    private String comment; // Nội dung đánh giá

    @Column(nullable = false)
    private int courtQualityRating;

    @Column(nullable = false)
    private int cleanlinessRating;

    @Column(nullable = false)
    private int bookingExperienceRating;

    private LocalDate playedDate;

    // Liên kết đến người dùng đánh giá
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    // Liên kết đến sân được đánh giá
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court_id")
    @JsonIgnore
    private Court court;
}
