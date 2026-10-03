package com.ridelink.ridemanagement.dto;

import com.ridelink.ridemanagement.enums.RideStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class RideResponse {
    private Long id;
    private Long passengerId;
    private Long driverId;
    private String pickupLocation;
    private String destinationLocation;
    private RideStatus status;
    private BigDecimal estimatedFare;
    private BigDecimal finalFare;
    private LocalDateTime createdAt;
    private LocalDateTime assignedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
}
