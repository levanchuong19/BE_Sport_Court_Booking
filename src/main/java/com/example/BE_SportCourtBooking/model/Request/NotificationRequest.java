package com.example.BE_SportCourtBooking.model.Request;

import com.example.BE_SportCourtBooking.entity.Enum.NotificationType;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
        private UUID accountId;
        private String title;
        private String message;
        private NotificationType type;
        private String redirectUrl;

}

