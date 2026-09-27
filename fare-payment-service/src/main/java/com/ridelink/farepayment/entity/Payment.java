package com.ridelink.farepayment.entity;

import jakarta.persistence.*;
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

    @Column(nullable = false)
    private UUID rideId; 

    @Column(nullable = false)
    private UUID passengerId;

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
