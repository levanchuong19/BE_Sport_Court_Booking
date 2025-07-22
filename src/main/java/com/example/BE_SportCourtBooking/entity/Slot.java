package com.example.BE_SportCourtBooking.entity;
import com.example.BE_SportCourtBooking.entity.Enum.BookingStatus;
import com.example.BE_SportCourtBooking.entity.Enum.PaymentMethod;
import com.example.BE_SportCourtBooking.entity.Enum.PriceType;
import com.example.BE_SportCourtBooking.entity.Enum.SlotStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "slots")
public class Slot {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    @JsonIgnore
    Account account;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "court_id", nullable = false)
    @JsonIgnore
    Court court;

    @OneToOne(mappedBy = "slot", fetch = FetchType.LAZY)
    @JsonIgnore
    private Payment payment;

    @Column(name = "create_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createAt;

    @NotNull
    LocalDate startDate;

    @NotNull
    LocalDate endDate;

    @NotNull(message = "Start time cannot be null!")
    @Column(nullable = false)
    @Pattern(regexp = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$",
            message = "Start time must be in HH:mm or HH:mm:ss format!")
    String startTime;

    @NotNull(message = "End time cannot be null!")
    @Column(nullable = false)
    @Pattern(regexp = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$",
            message = "End time must be in HH:mm or HH:mm:ss format!")
    String endTime;

    @NotNull(message = "Slot type is required!")
    @Enumerated(EnumType.STRING)
    @Column(name = "slot_type", nullable = false)
    PriceType slotType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    SlotStatus status;

    @Enumerated(EnumType.STRING)
    BookingStatus bookingStatus ;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    PaymentMethod paymentMethod;

    @Column(name = "is_deleted")
    Boolean isDelete = false;

    @Column(name = "reminder_sent")
    Boolean reminderSent = false;

    @Override
    public String toString() {
        return "Slot{id=" + id + ", startDate=" + startDate + ", endDate=" + endDate + ", bookingStatus=" + bookingStatus + "}";
        // Exclude 'account' and 'court' to avoid recursion
    }

}
