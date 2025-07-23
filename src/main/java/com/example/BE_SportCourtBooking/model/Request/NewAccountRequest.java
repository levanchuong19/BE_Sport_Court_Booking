package com.example.BE_SportCourtBooking.model.Request;

import com.example.BE_SportCourtBooking.entity.Enum.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewAccountRequest {
    @NotNull(message = "Full Name cannot be null!")
    @NotBlank(message = "Full Name cannot be blank!")
    @Column(name = "fullName", nullable = false)
    String fullName;

    @Column(name = "date_of_birth")
    Date dateOfBirth;

    @NotBlank(message = "Email cannot be blank!")
    @Email(message = "Invalid email")
    @Column(name = "email", unique = true, nullable = false)
    String email;

    @Pattern(regexp = "(84|0[3|5|7|8|9])+(\\d{8})\\b", message = "Invalid phone number")
    @Column(name = "phone", unique = true)
    String phone;

    @NotBlank(message = "Password cannot be blank!")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Column(name = "password", nullable = false)
    String password;

    @Column(name = "gender")
    String gender;

    @Column(name = "address")
    String address;

    @Column(name = "image")
    String image;

    @Enumerated(EnumType.STRING)
    Role role;

//    UUID managerId;

}
