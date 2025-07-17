package com.example.BE_SportCourtBooking.api;
import com.example.BE_SportCourtBooking.entity.BusinessLocation;
import com.example.BE_SportCourtBooking.entity.Field;
import com.example.BE_SportCourtBooking.model.Request.SearchRequest;
import com.example.BE_SportCourtBooking.model.Response.ApiResponse;
import com.example.BE_SportCourtBooking.model.Response.BusinessLocationResponse;
import com.example.BE_SportCourtBooking.service.SearchService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/")
@CrossOrigin("*")
@SecurityRequirement(name="api")
public class SearchAPI {
        @Autowired
        SearchService searchService;

        private ApiResponse createResponse(int code, boolean status, String message, Object data) {
            return new ApiResponse(code, status, message, data);
        }

        @PostMapping("search")
        public ResponseEntity<ApiResponse> searchFields(@RequestBody SearchRequest request) {

            try {
                List<BusinessLocationResponse> fields = searchService.searchLocations(request);
                return ResponseEntity.ok(createResponse(200, true, "Search successfully", fields));
            } catch (EntityNotFoundException e) {
                return ResponseEntity.status(404).body(createResponse(404, false, "Search error", e.getMessage()));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(createResponse(400, false, "Search error", e.getMessage()));
            } catch (IllegalStateException e) {
                return ResponseEntity.status(403).body(createResponse(403, false, "Search error", e.getMessage()));
            } catch (Exception e) {
                return ResponseEntity.status(500).body(createResponse(500, false, "Search error", e.getMessage()));
            }
        }
}
