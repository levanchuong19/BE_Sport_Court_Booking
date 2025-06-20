package com.example.BE_SportCourtBooking.api;

import com.example.BE_SportCourtBooking.entity.CourtFeedback;
import com.example.BE_SportCourtBooking.model.Request.FeedbackRequest;
import com.example.BE_SportCourtBooking.model.Response.ApiResponse;
import com.example.BE_SportCourtBooking.model.Response.CourtFeedbackResponse;
import com.example.BE_SportCourtBooking.service.CourtFeedbackService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@RequiredArgsConstructor
public class CourtFeedbackAPI {

    private final CourtFeedbackService courtFeedbackService;

    private ApiResponse createResponse(int code, boolean status, String message, Object data) {
        return new ApiResponse(code, status, message, data);
    }

    // Tạo feedback mới
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> createFeedback(@Valid @RequestBody FeedbackRequest request) {
        CourtFeedback feedback = courtFeedbackService.createFeedback(request);
        return ResponseEntity.ok(createResponse(200, true, "Feedback submitted", feedback));
    }

    // Lấy feedback theo sân
    @GetMapping("/court/{courtId}")
    public ResponseEntity<ApiResponse> getFeedbackByCourt(@PathVariable UUID courtId) {
        List<CourtFeedbackResponse> feedbacks = courtFeedbackService.getFeedbacksByCourt(courtId);
        return ResponseEntity.ok(createResponse(200, true, "Feedbacks found", feedbacks));
    }

    // Lấy feedback của user hiện tại
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getMyFeedbacks() {
        List<CourtFeedbackResponse> feedbacks = courtFeedbackService.getFeedbacksByCurrentAccount();
        return ResponseEntity.ok(createResponse(200, true, "Your feedbacks", feedbacks));
    }

}
