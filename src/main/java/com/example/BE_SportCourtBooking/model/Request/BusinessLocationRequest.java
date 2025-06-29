package com.example.BE_SportCourtBooking.model.Request;

import com.example.BE_SportCourtBooking.entity.Account;
import com.example.BE_SportCourtBooking.entity.Court;
import com.example.BE_SportCourtBooking.entity.Enum.LocationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.util.*;

@Data
public class BusinessLocationRequest {

    @NotNull(message = "Business Location cannot be null!")
    @NotBlank(message = "Business location cannot be blank!")
    @Column(nullable = false)
    String name;

    @NotNull(message = "Address name cannot be null!")
    @NotBlank(message = "Address name cannot be blank!")
    @Column(nullable = false)
    String address;

    String images;

    String description;

    @NotBlank(message = "Open time cannot be blank!")
    @Pattern(regexp = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$",
            message = "Open time must be in HH:mm or HH:mm:ss format!")
    String openTime;

    @NotBlank(message = "Close time cannot be blank!")
    @Pattern(regexp = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$",
            message = "Close time must be in HH:mm or HH:mm:ss format!")
    String closeTime;

    @NotNull(message = "Court number not null")
    Integer CourtNum;

    @NotNull(message = "Year build not null")
    Integer yearBuild;

    List<String> utilities;

    String businessLicense;

    @NotNull(message = "Owner cannot be null!")
    UUID owner; // Giả định Account là chủ sở hữu địa điểm


}
