package com.example.BE_SportCourtBooking.entity;

import com.example.BE_SportCourtBooking.entity.Enum.PaymentStatus;
import com.example.BE_SportCourtBooking.entity.Enum.PaymentType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.util.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "id", updatable = false, nullable = false)
    UUID id;

    @NotNull(message = "Slot is required!")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false)
    @JsonIgnore
    Slot slot;


    @NotNull(message = "Amount cannot be null!")
    @Column(name = "amount", nullable = false)
    BigDecimal amount;

    @NotNull(message = "Payment status is required!")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    PaymentType type;

    @NotNull(message = "Payment status is required!")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    PaymentStatus status;

    @Column(name = "payment_date")
    @CreationTimestamp
    Date createAt;

    BigDecimal total;

    @OneToMany(mappedBy = "payment",cascade = CascadeType.ALL)
    Set<Transaction> transactions = new HashSet<>();

    @Column(name = "vnpay_transaction_id")
    String vnpayTransactionId;

    @Override
    public String toString() {
        return "Payment{id=" + id + ", createAt=" + createAt + ", type=" +  type+ "}";
        // Omitting 'transactions' to avoid recursion
    }
    @Override
    public int hashCode() {
        // Use fields that uniquely identify the Payment
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Payment)) return false;
        Payment other = (Payment) obj;
        return Objects.equals(id, other.id); // Compare relevant fields
    }
}
