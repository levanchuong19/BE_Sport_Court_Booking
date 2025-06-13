package com.example.BE_SportCourtBooking.model.Request;

import com.example.BE_SportCourtBooking.entity.Enum.SupportContent;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SupportRequest {
    String fullName;
    String email;
    String phoneNumber;
    SupportContent content;
    String description;
}
