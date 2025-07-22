package com.example.BE_SportCourtBooking.api;
import com.example.BE_SportCourtBooking.entity.Court;
import com.example.BE_SportCourtBooking.entity.Enum.CourtStatus;
import com.example.BE_SportCourtBooking.entity.Enum.CourtType;
import com.example.BE_SportCourtBooking.model.Request.CourtPricingRequest;
import com.example.BE_SportCourtBooking.model.Request.CourtRequest;
import com.example.BE_SportCourtBooking.model.Request.CourtStatusRequest;
import com.example.BE_SportCourtBooking.model.Request.CourtUpdateRequest;
import com.example.BE_SportCourtBooking.model.Response.ApiResponse;
import com.example.BE_SportCourtBooking.model.Response.CourtResponse;
import com.example.BE_SportCourtBooking.service.CourtService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/court/")
@CrossOrigin("*")
@SecurityRequirement(name="api")
public class CourtAPI {

    @Autowired
    CourtService courtService;

    private ApiResponse createResponse(int code, boolean status, String message, Object data) {
        return new ApiResponse(code, status, message, data);
    }

    @PostMapping("create")
    public ResponseEntity<ApiResponse> createCourt(
            @Valid @RequestBody CourtRequest courtRequest) {
        try {
            return ResponseEntity.ok(createResponse(200, true, "Court created successfully", courtService.createCourt(courtRequest)));
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.status(404).body(createResponse(404, false, "Create Court error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createResponse(400, false, "Create Court error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(createResponse(403, false, "Create Court error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createResponse(500, false, "Create Court error", e.getMessage()));
        }
    }

    @GetMapping("getAll")
    public ResponseEntity<ApiResponse> getAllCourts(@RequestParam(required = false) CourtType courtType,
                                                    @RequestParam(required = false) CourtStatus status,
                                                    @RequestParam(required = false) String courtName,
                                                    @RequestParam(required = false) Boolean isDelete,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size)  {
        try {
            Page<CourtResponse> courts = courtService.getAllCourts(courtType, status, courtName, isDelete, page, size);

            return ResponseEntity.ok(createResponse(200, true, "Get all courts successfully", courts));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createResponse(500, false, "Get all courts error: " , e.getMessage()));
        }
    }

    @GetMapping("get/{courtId}")
    public ResponseEntity<ApiResponse> getCourt(UUID courtId) {
        try {
            return ResponseEntity.ok(createResponse(200, true, "Get courts successfully", courtService.getCourt(courtId)));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createResponse(500, false,  "Get court error: " ,e.getMessage()));
        }
    }

    @PatchMapping("{courtId}/status")
    public ResponseEntity<ApiResponse> updateCourtStatus(
            @PathVariable("courtId") UUID courtId,
            @Valid @RequestBody CourtStatusRequest statusRequest) {
        try {
            return ResponseEntity.ok(createResponse(200, true, "Update Court status successfully", courtService.updateCourtStatus(courtId, statusRequest)));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(createResponse(404, false, "Update Court status error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createResponse(400, false, "Update Court status error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(createResponse(403, false,  "Update Court status error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createResponse(500, false, "Update Court status error",  e.getMessage()));
        }
    }

    @PatchMapping("{courtId}/price")
    public ResponseEntity<ApiResponse> updateCourtPrice(
            @PathVariable("courtId") UUID courtId,
            @Valid @RequestBody List<@Valid CourtPricingRequest> priceRequests) {
        try {
            return ResponseEntity.ok(createResponse(200, true, "Update Court status successfully", courtService.updateCourtPrice(courtId, priceRequests)));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(createResponse(404, false, "Update Court status error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createResponse(400, false, "Update Court status error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(createResponse(403, false,  "Update Court status error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createResponse(500, false, "Update Court status error",  e.getMessage()));
        }
    }


    @DeleteMapping("delete/{courtId}")
    public ResponseEntity<ApiResponse> deleteCourt(@PathVariable UUID courtId){
        try{
            courtService.deleteCourt(courtId);
            return ResponseEntity.ok(createResponse(200, true, "Delete Court successfully", null));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(createResponse(404, false, "Delete Court error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createResponse(400, false, "Delete Court error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(createResponse(403, false,  "Delete Court error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createResponse(500, false, "Delete Court error",  e.getMessage()));
        }
    }

    @PutMapping("update/{courtId}")
    public ResponseEntity<ApiResponse> updateCourt(
            @PathVariable UUID courtId,
            @Valid @RequestBody CourtUpdateRequest courtRequest) {
        try {
            return ResponseEntity.ok(createResponse(200, true, "Update Court successfully", courtService.updateCourt(courtId, courtRequest)));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(createResponse(404, false, "Update Court error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createResponse(400, false, "Update Court error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(createResponse(403, false,  "Update Court error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createResponse(500, false, "Update Court error",  e.getMessage()));
        }
    }

    @DeleteMapping("deleteImage/{courtId}/{imageId}")
    public ResponseEntity<ApiResponse> deleteImage(
            @PathVariable UUID courtId,
            @PathVariable UUID imageId) {
        try {
            courtService.deleteImage(courtId, imageId);
            return ResponseEntity.ok(createResponse(200, true, "Delete Court image successfully", null));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(createResponse(404, false, "Delete Court image error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createResponse(400, false, "Delete Court image error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(createResponse(403, false,  "Delete Court image error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createResponse(500, false, "Delete Court image error",  e.getMessage()));
        }
    }

    @GetMapping("{courtId}/getImages")
    public ResponseEntity<ApiResponse> getImages(@PathVariable UUID courtId) {
        try {
            return ResponseEntity.ok(createResponse(200, true, "Get Images Court successfully", courtService.getImagesByCourt(courtId)));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createResponse(500, false, "Get Images Court error: " , e.getMessage()));
        }
    }

    @PostMapping("{courtId}/images")
    public ResponseEntity<ApiResponse> addImages(@PathVariable UUID courtId, @RequestBody List<String> newImageUrls) {
        try{
        courtService.addImagesToCourt(courtId, newImageUrls);
        return ResponseEntity.ok(createResponse(200, true, "Add Images to Court successfully", null));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(createResponse(404, false, "Add Images to Court error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createResponse(400, false, "Add Images to Court error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(createResponse(403, false,  "Add Images to Court error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createResponse(500, false, "Add Images to Court error",  e.getMessage()));
        }

    }

    @GetMapping("getByBusinessLocation/{businessLocationId}")
    public ResponseEntity<ApiResponse> getCourtsByBusinessLocation(@PathVariable UUID businessLocationId,
                                                               @RequestParam(required = false) Boolean isDelete,
                                                               @RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "10") int size) {
        try {
            Page<CourtResponse> courts = courtService.getCourtsByBusinessLocation(businessLocationId,isDelete, page, size);
            return ResponseEntity.ok(createResponse(200, true, "Get Courts by Business Location successfully", courts));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(createResponse(404, false, "Get Courts by Business Location error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createResponse(400, false, "Get Courts by Business Location error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(createResponse(403, false,  "Get Courts by Business Location error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createResponse(500, false, "Get Courts by Business Location error: " , e.getMessage()));
        }
    }

    @GetMapping("/top3-court-bookings")
    public ResponseEntity<ApiResponse> getTop3CourtsBookings() {
        try {
            List<CourtResponse> list = courtService.getTop3CourtsByBookingCount();
            return ResponseEntity.ok(createResponse(200, true, "Get top 3 Courts Bookings successfully", list));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(createResponse(404, false, "Get top 3 Courts Bookings error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createResponse(400, false, "Get top 3 Courts Bookings error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(createResponse(403, false, "Get top 3 Courts Bookings error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createResponse(500, false, "Get top 3 Courts Bookings error: ", e.getMessage()));
        }
    }

    @GetMapping("getCourtByOwner/{ownerId}")
    public ResponseEntity<ApiResponse> getCourtByOwner(@PathVariable UUID ownerId) {
        try {
            List<Court> courts = courtService.getCourtByOwner(ownerId);
            return ResponseEntity.ok(createResponse(200, true, "Get Court by Owner successfully", courts));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(createResponse(404, false, "Get Court by Owner error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createResponse(400, false, "Get Court by Owner error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(createResponse(403, false,  "Get Court by Owner error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createResponse(500, false, "Get Court by Owner error: " , e.getMessage()));
        }
    }
}
