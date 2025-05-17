package com.example.BE_SportCourtBooking.model.Request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotNull(message = "First Name cannot be null!")
    @Column(name = "first_name", nullable = false)
     String fullName;

    @Email(message = "Invalid email format!")
    @Column(unique = true)
    String email;

    @Pattern(regexp = "(84|0[3|5|7|8|9])+(\\d{8})\\b" , message = "Invalid phone number")
    @Column(unique = true)
    String phone;

//    @NotBlank(message = "Password is required!")
    @Size(min = 6 , message = "Password must be exceed 6 characters ")
    String password;

    @NotBlank(message = "Confirm Password is required!")
    @Size(min = 6 , message = "Confirm Password must be at least 6 characters long!")
    String confirmPassword;
}
