package com.example.BE_SportCourtBooking.api;

import com.example.BE_SportCourtBooking.entity.Account;
import com.example.BE_SportCourtBooking.model.Request.LoginRequest;
import com.example.BE_SportCourtBooking.model.Request.RegisterRequest;
import com.example.BE_SportCourtBooking.model.Response.AccountResponse;
import com.example.BE_SportCourtBooking.model.Response.ApiResponse;
import com.example.BE_SportCourtBooking.service.AuthenticationService;
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
        AccountResponse accountResponse = authenticationService.register(registerRequest);
        ApiResponse response = createResponse(200, true, "Registration successfully", accountResponse);
        return ResponseEntity.ok(response);
    }

    @PostMapping("login")
    public ResponseEntity login (@Valid @RequestBody LoginRequest loginRequest){
        AccountResponse accountResponse = authenticationService.login(loginRequest);
        ApiResponse response = createResponse(200, true, "Login successfully", accountResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("account")
    public ResponseEntity getAllAccounts() {
        List<Account> accounts = authenticationService.getAllAccounts();
        ApiResponse response = createResponse(200, true, "Accounts retrieved successfully", accounts);
        return ResponseEntity.ok(response);
    }

    @GetMapping("account/{id}")
    public ResponseEntity<ApiResponse> getAccount(@PathVariable UUID id){
        Account account = authenticationService.getAccount(id);
        ApiResponse response = createResponse(200, true, "Account retrieved successfully", account);
        return ResponseEntity.ok(response);
    }
}
