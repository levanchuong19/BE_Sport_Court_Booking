package com.example.BE_SportCourtBooking.api;

import com.example.BE_SportCourtBooking.entity.Account;
import com.example.BE_SportCourtBooking.model.Request.NotificationRequest;
import com.example.BE_SportCourtBooking.model.Response.NotificationResponse;
import com.example.BE_SportCourtBooking.service.AccountService;
import com.example.BE_SportCourtBooking.service.NotificationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@RequiredArgsConstructor
public class NotificationAPI {

    private final NotificationService notificationService;
    private final AccountService accountService;

    @PostMapping("/create")
    public ResponseEntity<NotificationResponse> createNotification(@RequestBody NotificationRequest request) {
        try {
            Account account = accountService.getAccount(request.getAccountId());
            NotificationResponse response = notificationService.sendNotification(
                    account,
                    request.getTitle(),
                    request.getMessage(),
                    request.getType(),
                    request.getRedirectUrl()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<NotificationResponse>> getAllNotificationsForCurrentUser() {
        Account currentAccount = accountService.getCurrentAccount();
        List<NotificationResponse> notifications = notificationService.getAllNotifications(currentAccount);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications() {
        Account currentAccount = accountService.getCurrentAccount();
        List<NotificationResponse> unread = notificationService.getUnreadNotifications(currentAccount);
        return ResponseEntity.ok(unread);
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable UUID id) {
        try {
            NotificationResponse response = notificationService.markAsRead(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/read-all")
    public ResponseEntity<String> markAllAsRead() {
        Account currentAccount = accountService.getCurrentAccount();
        notificationService.markAllAsRead(currentAccount);
        return ResponseEntity.ok("Đã đánh dấu tất cả là đã đọc.");
    }
}
