package com.example.BE_SportCourtBooking.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
public class Field {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "id", updatable = false, nullable = false)
    UUID id;
    String name;
    String address;
    double latitude;
    double longitude;
    double distance;
}
