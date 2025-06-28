package com.example.BE_SportCourtBooking.entity;

import com.example.BE_SportCourtBooking.entity.Enum.TransactionEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(name = "transaction_date")
    @CreationTimestamp
    Date transactionDate;

    @Enumerated(EnumType.STRING)
    TransactionEnum status;

    @Column(name = "description")
    String description;

    @NotNull(message = "Amount cannot be null!")
    @Column(name = "amount", nullable = false)
    BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "from_id")
    Account from;

    @ManyToOne
    @JoinColumn(name = "to_id")
    Account to;

    @NotNull(message = "Payment is required!")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    @JsonIgnore
    Payment payment;

    @Override
    public String toString() {
        return "Transaction{id=" + id + ", transactionDate=" + transactionDate + ", status=" + status + ", description='" + description + "'', amount=\" + amount + \"}";
        // Omitting 'payment' to avoid recursion
    }


    @Override
    public int hashCode() {
        // Use fields that uniquely identify the Transaction
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Transaction)) return false;
        Transaction other = (Transaction) obj;
        return Objects.equals(transactionDate, other.transactionDate) && // Compare other relevant fields
                Objects.equals(from, other.from) &&
                Objects.equals(to, other.to) &&
                Objects.equals(status, other.status) &&
                Objects.equals(description, other.description);
    }
}
