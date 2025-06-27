package com.example.BE_SportCourtBooking.api;

import com.example.BE_SportCourtBooking.entity.Slot;
import com.example.BE_SportCourtBooking.model.Response.ApiResponse;
import com.example.BE_SportCourtBooking.service.PaymentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payment/")
@CrossOrigin("*")
@SecurityRequirement(name="api")
public class PaymentAPI {

    @Autowired
    PaymentService paymentService;

    private ApiResponse createResponse(int code, boolean status, String message, Object data) {
        return new ApiResponse(code, status, message, data);
    }

    @PostMapping("createURL/{slotId}")
    public ResponseEntity<ApiResponse> createPaymentUrl(@PathVariable UUID slotId) {
        try {
            String url = paymentService.createUrl(slotId);
            return ResponseEntity.ok(createResponse(200, true, "Create payment URL successfully", url));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createResponse(400, false, "Create payment URL error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createResponse(500, false, "Create payment URL error", e.getMessage()));
        }
    }

    @PostMapping("transaction/{slotId}")
    public ResponseEntity<ApiResponse> createTransaction(@PathVariable UUID slotId) {
        try {
            paymentService.createTransaction(slotId);
            return ResponseEntity.ok(createResponse(200, true, "Create transaction successfully", null));
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.status(404).body(createResponse(404, false, "Create transaction error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createResponse(400, false, "Create transaction error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(createResponse(403, false, "Create transaction error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createResponse(500, false, "Create transaction error", e.getMessage()));
        }
    }
}
