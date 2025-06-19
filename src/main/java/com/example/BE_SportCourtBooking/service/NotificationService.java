package com.example.BE_SportCourtBooking.service;

import com.example.BE_SportCourtBooking.entity.Account;
import com.example.BE_SportCourtBooking.entity.Enum.NotificationType;
import com.example.BE_SportCourtBooking.entity.Notification;
import com.example.BE_SportCourtBooking.model.Response.NotificationResponse;
import com.example.BE_SportCourtBooking.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationResponse sendNotification(Account account, String title, String message, NotificationType type, String redirectUrl) {
        Notification notification = Notification.builder()
                .account(account)
                .title(title)
                .message(message)
                .type(type)
                .redirectUrl(redirectUrl)
                .isRead(false)
                .build();
        return NotificationResponse.fromEntity(notificationRepository.save(notification));
    }

    public List<NotificationResponse> getAllNotifications(Account account) {
        return notificationRepository.findByAccountOrderByCreatedAtDesc(account)
                .stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<NotificationResponse> getUnreadNotifications(Account account) {
        return notificationRepository.findByAccountAndIsReadFalse(account)
                .stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public NotificationResponse markAsRead(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        return NotificationResponse.fromEntity(notificationRepository.save(notification));
    }

    public void markAllAsRead(Account account) {
        List<Notification> unread = notificationRepository.findByAccountAndIsReadFalse(account);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

}
