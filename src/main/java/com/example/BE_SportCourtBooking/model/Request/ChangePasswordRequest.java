package com.example.BE_SportCourtBooking.model.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequest {
    private String password;
    private String newPassword;
    private String confirmNewPassword;
}
