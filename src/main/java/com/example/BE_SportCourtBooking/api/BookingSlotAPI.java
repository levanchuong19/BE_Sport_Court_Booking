package com.example.BE_SportCourtBooking.api;

import com.example.BE_SportCourtBooking.entity.Enum.PriceType;
import com.example.BE_SportCourtBooking.entity.Enum.SlotStatus;
import com.example.BE_SportCourtBooking.entity.Slot;
import com.example.BE_SportCourtBooking.model.Request.SlotRequest;
import com.example.BE_SportCourtBooking.model.Response.ApiResponse;
import com.example.BE_SportCourtBooking.service.SlotService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/slot/")
@CrossOrigin("*")
@SecurityRequirement(name="api")
public class BookingSlotAPI {

    @Autowired
    SlotService slotService;

    private ApiResponse createResponse(int code, boolean status, String message, Object data) {
        return new ApiResponse(code, status, message, data);
    }

    @PostMapping("create")
    public ResponseEntity<ApiResponse> createSlot(@Valid @RequestBody SlotRequest slotRequest) {
        try {
            return ResponseEntity.ok(createResponse(200, true, "Created booking successfully", slotService.createSlot(slotRequest)));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(createResponse(404, false, "Create booking error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createResponse(400, false, "Create booking error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(createResponse(403, false, "Create booking error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createResponse(500, false, "Create booking error", e.getMessage()));
        }
    }

    @GetMapping("getAll")
    public ResponseEntity<ApiResponse> getAllSlots(@RequestParam(required = false) PriceType slotType,
                                                   @RequestParam(required = false)SlotStatus slotStatus,
                                                   @RequestParam(required = false) Boolean isDelete,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size)  {
        try {
            Page<Slot> slot = slotService.getAllSlot(slotType,slotStatus,isDelete, page, size);
            return ResponseEntity.ok(createResponse(200, true, "Get all slot successfully", slot));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createResponse(500, false, "Get all slot error: " ,e.getMessage()));
        }
    }

    @GetMapping("get/{id}")
    public ResponseEntity<ApiResponse> getSlotById(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(createResponse(200, true, "Get slot successfully", slotService.getSlot(id)));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(createResponse(404, false, "Get slot error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createResponse(500, false, "Get slot error", e.getMessage()));
        }
    }

    @PatchMapping("{slotId}/checkIn")
    public ResponseEntity<ApiResponse> checkInCourt(@PathVariable("slotId") UUID slotId) {
        try {
            return ResponseEntity.ok(createResponse(200, true, "CheckIn successfully", slotService.checkIn(slotId)));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(createResponse(404, false, "CheckIn error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createResponse(400, false, "CheckIn error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(createResponse(403, false,  "CheckIn error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createResponse(500, false, "CheckIn error",  e.getMessage()));
        }
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<ApiResponse> deleteSlot(@PathVariable UUID id) {
        try {
            slotService.deleteSlot(id);
            return ResponseEntity.ok(createResponse(200, true, "Delete slot successfully", null));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(createResponse(404, false, "Delete slot error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(createResponse(403, false, "Delete slot error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createResponse(500, false, "Delete slot error", e.getMessage()));
        }
    }

    @PutMapping("update/{id}")
    public ResponseEntity<ApiResponse> updateSlot(@PathVariable UUID id, @Valid @RequestBody SlotRequest slotRequest) {
        try {
            return ResponseEntity.ok(createResponse(200, true, "Update slot successfully", slotService.updateSlot(id, slotRequest)));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(createResponse(404, false, "Update slot error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createResponse(400, false, "Update slot error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(createResponse(403, false, "Update slot error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createResponse(500, false, "Update slot error", e.getMessage()));
        }
    }

    @GetMapping("getBookingByAccount/{accountId}")
    public ResponseEntity<ApiResponse> getBookingHistory(@PathVariable UUID accountId){
        try {
            return ResponseEntity.ok(createResponse(200, true, "Get Booking History successfully", slotService.getBookingHistory(accountId)));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(createResponse(404, false, "Get Booking History error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createResponse(400, false, "Get Booking History error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(createResponse(403, false, "Get Booking History error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createResponse(500, false, "Get Booking History error", e.getMessage()));
        }
    }
}
