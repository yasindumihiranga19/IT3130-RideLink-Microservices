package com.ridelink.farepayment.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Data
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "Ride ID is required")
    @Column(nullable = false)
    private Long rideId; 

    @NotNull(message = "Passenger ID is required")
    @Column(nullable = false)
    private Long passengerId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Column(nullable = false)
    private BigDecimal amount;

    // To show that this is in Rupees
    private String currency;

    // e.g., PENDING, SUCCESS, FAILED
    @Column(nullable = false)
    private String status; 

    // e.g., CREDIT_CARD, CASH
    private String paymentMethod; 

    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
