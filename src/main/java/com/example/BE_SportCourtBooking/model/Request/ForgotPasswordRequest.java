package com.example.BE_SportCourtBooking.model.Request;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class ForgotPasswordRequest {
    @Email
    String email;
}
