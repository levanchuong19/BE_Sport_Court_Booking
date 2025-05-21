package com.example.BE_SportCourtBooking.api;

import com.example.BE_SportCourtBooking.model.Request.LoginRequest;
import com.example.BE_SportCourtBooking.model.Request.RegisterRequest;
import com.example.BE_SportCourtBooking.model.Response.AccountResponse;
import com.example.BE_SportCourtBooking.model.Response.ApiResponse;
import com.example.BE_SportCourtBooking.service.AccountService;
import com.example.BE_SportCourtBooking.service.AuthenticationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/")
@CrossOrigin("*")
@SecurityRequirement(name="api")
public class AuthenticationAPI {
    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    AccountService accountService;

    private ApiResponse createResponse(int code, boolean status, String message, Object data) {
        return new ApiResponse(code, status, message, data);
    }
    @PostMapping("register")
    public ResponseEntity register (@Valid @RequestBody RegisterRequest registerRequest){
        try {
            AccountResponse accountResponse = authenticationService.register(registerRequest);
            ApiResponse response = createResponse(200, true, "Registration successfully", accountResponse);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse response = createResponse(400, false, null, e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("login")
    public ResponseEntity login (@Valid @RequestBody LoginRequest loginRequest){
        try {
            AccountResponse accountResponse = authenticationService.login(loginRequest);
            ApiResponse response = createResponse(200, true, "Login successfully", accountResponse);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse response = createResponse(400, false, null, e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}
