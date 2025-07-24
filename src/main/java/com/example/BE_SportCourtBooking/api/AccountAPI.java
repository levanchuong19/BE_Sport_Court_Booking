package com.example.BE_SportCourtBooking.api;

import com.example.BE_SportCourtBooking.entity.Account;
import com.example.BE_SportCourtBooking.model.Request.NewAccountRequest;
import com.example.BE_SportCourtBooking.model.Request.UpdateAccountRequest;
import com.example.BE_SportCourtBooking.model.Request.UpdateAccountStatusRequest;
import com.example.BE_SportCourtBooking.model.Response.ApiResponse;
import com.example.BE_SportCourtBooking.model.Response.GetAccountResponse;
import com.example.BE_SportCourtBooking.service.AccountService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
@RestController
@RequestMapping("/api/account")
@CrossOrigin("*")
@SecurityRequirement(name="api")
public class AccountAPI {
    @Autowired
    AccountService accountService;

    private ApiResponse createResponse(int code, boolean status, String message, Object data) {
        return new ApiResponse(code, status, message, data);
    }

    @PostMapping
    public ResponseEntity<Account> create(@Valid @RequestBody NewAccountRequest newAccountRequest,
                                          @RequestParam(required = false) UUID managerId) {
        Account newBreed = accountService.createAccount(newAccountRequest, managerId);
        return ResponseEntity.ok(newBreed);
    }

    @PutMapping("{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER', 'MANAGER')")
    public ResponseEntity<Account> update(@Valid @RequestBody UpdateAccountRequest updateAccountRequest, @PathVariable UUID id) {
        Account updatedAccount = accountService.updateAccount(updateAccountRequest, id);
        return ResponseEntity.ok(updatedAccount);
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<GetAccountResponse>> getAll() {
        List<GetAccountResponse> accountResponses = accountService.getAllAccounts();

        return ResponseEntity.ok(accountResponses);
    }

    @GetMapping("accountDetail/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<ApiResponse> getAccountDetail(@PathVariable UUID id) {
        try {
            Account account = accountService.getAccount(id);
            ApiResponse response = createResponse(200, true, "Account retrieved successfully", account);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse response = createResponse(400, false, null, e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @DeleteMapping("{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable UUID id) {
        try {
            Account deletedAccount = accountService.deleteAccount(id);
            ApiResponse response = createResponse(200, true, "Account deleted successfully", deletedAccount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateAccountStatus(
            @PathVariable UUID id,
            @RequestBody UpdateAccountStatusRequest request
    ) {
        accountService.updateAccountStatus(id, request);
        return ResponseEntity.ok("Cập nhật trạng thái thành công");
    }
}
