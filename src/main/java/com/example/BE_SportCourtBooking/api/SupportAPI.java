package com.example.BE_SportCourtBooking.api;

import com.example.BE_SportCourtBooking.model.Request.SupportRequest;
import com.example.BE_SportCourtBooking.model.Response.ApiResponse;
import com.example.BE_SportCourtBooking.model.Response.SupportResponse;
import com.example.BE_SportCourtBooking.service.SupportService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/support/")
@CrossOrigin("*")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SupportAPI {

    SupportService supportService;

    @PostMapping("create")
    public ResponseEntity<ApiResponse> createSupport(@RequestBody SupportRequest request) {
        SupportResponse support = supportService.createSupport(request);
        return ResponseEntity.ok(new ApiResponse(200, true, "Create support successfully !", support));
    }

    @GetMapping("getAll")
    @PreAuthorize("hasRole('MANAGER')")
    @SecurityRequirement(name = "api")
    public ResponseEntity<ApiResponse> getAllSupport() {
        List<SupportResponse> supportResponse = supportService.getAllFeedback();
        return ResponseEntity.ok(new ApiResponse(200, true, "Get All Support Successfully !", supportResponse));
    }

    @GetMapping("getByID/{supportId}")
    @PreAuthorize("hasRole('MANAGER')")
    @SecurityRequirement(name = "api")
    public ResponseEntity<ApiResponse> getSupportById(@PathVariable UUID supportId) {
        SupportResponse supportResponse = supportService.getFeedbackById(supportId);
        return ResponseEntity.ok(new ApiResponse(200, true, "Get Support Successfully !", supportResponse));
    }

    @DeleteMapping("delete/{supportId}")
    @PreAuthorize("hasRole('MANAGER')")
    @SecurityRequirement(name = "api")
    public ResponseEntity<ApiResponse> deleteSupport(@PathVariable("supportId") UUID id) {
        SupportResponse support = supportService.deleteSupport(id);
        return ResponseEntity.ok(new ApiResponse(200, true, "Delete support successfully !", support));
    }
}
