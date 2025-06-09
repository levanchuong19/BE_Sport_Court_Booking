package com.example.BE_SportCourtBooking.entity;

import com.example.BE_SportCourtBooking.entity.Enum.SupportContent;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "supports")
public class Support {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "id", updatable = false, nullable = false)
    UUID id;

    @NotNull(message = "Full Name cannot be null!")
    @NotBlank(message = "Full Name cannot be blank!")
    @Column(name = "fullName", nullable = false)
    String fullName;

    @NotBlank(message = "Email cannot be blank!")
    @Email(message = "Invalid email")
    @Column(name = "email", unique = true, nullable = false)
    String email;

    @Pattern(regexp = "(84|0[3|5|7|8|9])+(\\d{8})\\b", message = "Invalid phone number")
    @Column(name = "phone", unique = true)
    String phone;

    @NotNull(message = "Content cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "content", nullable = false)
    SupportContent content;

    @Column(name = "description")
    String description;

}
