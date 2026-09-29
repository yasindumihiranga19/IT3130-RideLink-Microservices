package com.ridelink.ridemanagement.dto;

import com.ridelink.ridemanagement.model.RideStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RideResponseDto {
    private Long id;
    private Long passengerId;
    private Long driverId;
    private String pickupLocation;
    private String destination;
    private RideStatus status;
    private Double fare;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
