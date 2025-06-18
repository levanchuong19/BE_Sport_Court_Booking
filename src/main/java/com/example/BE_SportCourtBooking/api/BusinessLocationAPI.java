package com.example.BE_SportCourtBooking.api;

import com.example.BE_SportCourtBooking.entity.BusinessLocation;
import com.example.BE_SportCourtBooking.model.Request.BusinessLocationRequest;
import com.example.BE_SportCourtBooking.model.Response.ApiResponse;
import com.example.BE_SportCourtBooking.model.Response.BusinessLocationResponse;
import com.example.BE_SportCourtBooking.model.Response.CourtResponse;
import com.example.BE_SportCourtBooking.service.BusinessLocationService;
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
@RequestMapping("/api/location/")
@CrossOrigin("*")
@SecurityRequirement(name="api")
public class BusinessLocationAPI {
    @Autowired
    BusinessLocationService businessLocationService;

    private ApiResponse createResponse(int code, boolean status, String message, Object data) {
        return new ApiResponse(code, status, message, data);
    }

    @PostMapping("create")
    public ResponseEntity<ApiResponse> createCourt(
            @Valid @RequestBody BusinessLocationRequest businessLocationRequest) {
        try {
            businessLocationService.createBusinessLocation(businessLocationRequest);
            return ResponseEntity.ok(createResponse(200, true, "Business location created successfully", null));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(createResponse(404, false, "Create Business location error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createResponse(400, false, "Create Business location error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(createResponse(403, false, "Create Business location error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createResponse(500, false, "Create Business location error", e.getMessage()));
        }
    }

    @GetMapping("getAll")
    public ResponseEntity<ApiResponse> getAll(@RequestParam(required = false) String name,
                                                    @RequestParam(required = false) String address,
                                                    @RequestParam(required = false) Boolean isDelete,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size)  {
        try {
            Page<BusinessLocation> businessLocations = businessLocationService.getAllWithPagination(name, address, isDelete, page, size);
            businessLocations.getContent().forEach(bl -> {
                System.out.println("BusinessLocation ID: " + bl.getId() + ", Courts size: " + bl.getCourts().size());
            });
            return ResponseEntity.ok(createResponse(200, true, "Get all Business Location successfully", businessLocations));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createResponse(500, false, null, "Get all Business Location error: " + e.getMessage()));
        }
    }

    @GetMapping("getById/{id}")
    public ResponseEntity<ApiResponse> getBusinessLocationById(@PathVariable UUID id) {
        try {
            BusinessLocation businessLocation = businessLocationService.getById(id);
            return ResponseEntity.ok(createResponse(200, true, "Get Business Location by ID successfully", businessLocation));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(createResponse(404, false, "Business Location not found", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createResponse(500, false, "Get Business Location by ID error", e.getMessage()));
        }
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<ApiResponse> deleteBusinessLocation(@PathVariable UUID id) {
        try {
            businessLocationService.deleteBusinessLocation(id);
            return ResponseEntity.ok(createResponse(200, true, "Business Location deleted successfully", null));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(createResponse(404, false, "Business Location not found", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(createResponse(403, false, "Delete Business Location error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createResponse(500, false, "Delete Business Location error", e.getMessage()));
        }
    }

    @PutMapping("update/{id}")
    public ResponseEntity<ApiResponse> updateBusinessLocation(@PathVariable UUID id,
                                                               @Valid @RequestBody BusinessLocationRequest request) {
        try {
            BusinessLocationResponse updatedLocation = businessLocationService.updateBusinessLocation(id, request);
            return ResponseEntity.ok(createResponse(200, true, "Business Location updated successfully", updatedLocation));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(createResponse(404, false, "Business Location not found", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createResponse(400, false, "Update Business Location error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createResponse(500, false, "Update Business Location error", e.getMessage()));
        }
    }

    @GetMapping("getCourtsByOwnerId/{ownerId}")
    public ResponseEntity<ApiResponse> getCourtsByOwnerId(@PathVariable UUID ownerId,
                                                           @RequestParam(required = false) Boolean isDelete,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size) {
        try {
            Page<BusinessLocationResponse> businessLocations = businessLocationService.getBusinessLocationsByOwnerId(ownerId, isDelete, page, size);
            return ResponseEntity.ok(createResponse(200, true, "Get Courts by Owner ID successfully", businessLocations));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(createResponse(404, false, "Get Courts by Owner ID error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createResponse(400, false, "Get Courts by Owner ID error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createResponse(500, false, "Get Courts by Owner ID error", e.getMessage()));
        }
    }
    @GetMapping("/top3-BusinessLocations")
    public ResponseEntity<ApiResponse> getTop3BusinessLocations() {
        try {
            List<BusinessLocationResponse> list = businessLocationService.getTop3BusinessLocationsByBookingCount();
            return ResponseEntity.ok(createResponse(200, true, "Get top 3 Business Locations successfully", list));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(createResponse(404, false, "Get top 3 Business Locations error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createResponse(400, false, "Get top 3 Business Locations error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(createResponse(403, false, "Get top 3 Business Locations error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createResponse(500, false, "Get top 3 Business Locations error: ", e.getMessage()));
        }
    }
}
