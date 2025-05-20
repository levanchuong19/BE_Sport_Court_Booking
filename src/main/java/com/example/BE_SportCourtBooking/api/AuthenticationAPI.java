package com.example.BE_SportCourtBooking.api;

import com.example.BE_SportCourtBooking.entity.Account;
import com.example.BE_SportCourtBooking.model.Request.ChangePasswordRequest;
import com.example.BE_SportCourtBooking.model.Request.ForgotPasswordRequest;
import com.example.BE_SportCourtBooking.model.Request.LoginRequest;
import com.example.BE_SportCourtBooking.model.Request.RegisterRequest;
import com.example.BE_SportCourtBooking.model.Response.AccountResponse;
import com.example.BE_SportCourtBooking.model.Response.ApiResponse;
import com.example.BE_SportCourtBooking.model.Response.ChangePasswordResponse;
import com.example.BE_SportCourtBooking.model.Response.ForgotPasswordResponse;
import com.example.BE_SportCourtBooking.service.AuthenticationService;
import com.example.BE_SportCourtBooking.service.EmailService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/")
@CrossOrigin("*")
@SecurityRequirement(name="api")
public class AuthenticationAPI {
    @Autowired
    AuthenticationService authenticationService;

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

    @GetMapping("account")
    public ResponseEntity getAllAccounts() {
        try{
        List<Account> accounts = authenticationService.getAllAccounts();
        ApiResponse response = createResponse(200, true, "Accounts retrieved successfully", accounts);
        return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse response = createResponse(400, false, null, e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("account/{id}")
    public ResponseEntity<ApiResponse> getAccount(@PathVariable UUID id){
        try{
        Account account = authenticationService.getAccount(id);
        ApiResponse response = createResponse(200, true, "Account retrieved successfully", account);
        return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse response = createResponse(400, false, null, e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        try {
            ForgotPasswordResponse forgotPasswordResponse = authenticationService.forgotPassword(forgotPasswordRequest);
            ApiResponse response = createResponse(200, true, "Send successfully", forgotPasswordResponse);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse response = createResponse(400, false, "Sending email failed", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(
            @RequestBody ChangePasswordRequest changePasswordRequest,
            @RequestHeader("accountId") UUID accountId) { // giả sử bạn truyền accountId trong header

        try {
            ChangePasswordResponse changePasswordResponse = authenticationService.changePassword(changePasswordRequest, accountId);
            ApiResponse response = createResponse(200, true, "Change password successfully", changePasswordResponse);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse response = createResponse(400, false, "Change password failed", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}
