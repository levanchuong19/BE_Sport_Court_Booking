package com.example.BE_SportCourtBooking.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "business_location")
public class BusinessLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @NotNull(message = "Business Location cannot be null!")
    @NotBlank(message = "Business location cannot be blank!")
    @Column(nullable = false)
    String name;

    @NotNull(message = "Address name cannot be null!")
    @NotBlank(message = "Address name cannot be blank!")
    @Column(nullable = false)
    String address;

    @NotBlank(message = "Open time cannot be blank!")
    @Pattern(regexp = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$",
            message = "Open time must be in HH:mm or HH:mm:ss format!")
    String openTime;

    @NotBlank(message = "Close time cannot be blank!")
    @Pattern(regexp = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$",
            message = "Close time must be in HH:mm or HH:mm:ss format!")
    String closeTime;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    Account owner;

    @OneToMany(mappedBy = "businessLocation",fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Court> courts;

    @Column(name = "is_deleted", nullable = false)

    Boolean isDelete = false;
}
