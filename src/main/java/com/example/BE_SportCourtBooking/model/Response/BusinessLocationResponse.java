package com.example.BE_SportCourtBooking.model.Response;

import com.example.BE_SportCourtBooking.entity.Court;
import com.example.BE_SportCourtBooking.entity.Enum.LocationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessLocationResponse {
    private UUID id;
    private String name;
    private String address;
    private String openTime;
    private String closeTime;
    private AccountResponse owner;
    Double latitude;
    Double longitude;
    Set<CourtResponse> courts;
    Integer CourtNum;
    Integer yearBuild;
    List<String> utilities;
    String businessLicense;
    Date createAt;
    LocationStatus status ;
    String images;
    String description;
    Date modifiedAt;
    Double distance;
}
