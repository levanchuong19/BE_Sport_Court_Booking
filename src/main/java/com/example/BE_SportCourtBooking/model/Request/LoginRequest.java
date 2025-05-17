package com.example.BE_SportCourtBooking.model.Request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginRequest {

    @Pattern(regexp = "(84|0[3|5|7|8|9])+(\\d{8})\\b" , message = "Invalid phone number")
    @Column(unique = true)
    String phone;

    @NotBlank(message = "Password is required!")
    @Size(min = 6 , message = "Password must be exceed 6 characters ")
    String password;
}
