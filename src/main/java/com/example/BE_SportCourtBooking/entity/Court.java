package com.example.BE_SportCourtBooking.entity;

import com.example.BE_SportCourtBooking.entity.Enum.CourtStatus;
import com.example.BE_SportCourtBooking.entity.Enum.CourtType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "courts")
public class Court {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "id", updatable = false, nullable = false)
    UUID id;

    @NotNull(message = "Court type is required!")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    CourtType courtType;

    @NotNull(message = "Court name cannot be null!")
    @NotBlank(message = "Court name cannot be blank!")
    @Column(name = "courtName", nullable = false)
    String courtName;

    @NotBlank(message = "Description cannot be blank!")
    @Column(name = "description", nullable = false)
    String description;

    @NotNull(message = "Construction year cannot be null!")
    @Column(name = "construction_year")
    Integer yearBuild;

    @NotNull(message = "Length cannot be null!")
    @Column(name = "length")
    Double length;

    @NotNull(message = "Width cannot be null!")
    @Column(name = "width")
    Double width;

    @NotNull(message = "Maximum number of players cannot be null!")
    @Column(name = "max_players")
    Integer maxPlayers;

    @OneToMany(mappedBy = "court", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Image> images;

    @NotNull(message = "Court status is required!")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    CourtStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    @NotNull(message = "Court must have a manager account!")
    Account courtManager;

    @OneToMany(mappedBy = "court", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Slot> slots;

    @OneToMany(mappedBy = "court", cascade = CascadeType.ALL, orphanRemoval = true)
    List<CourtPricing> prices;

    @Column(name = "is_deleted")
    Boolean isDelete = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "business_location_id", nullable = false)
    @NotNull(message = "Court must have a manager account!")
    BusinessLocation businessLocation;

    @OneToMany(mappedBy = "court", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<CourtFeedback> feedbacks;

}
