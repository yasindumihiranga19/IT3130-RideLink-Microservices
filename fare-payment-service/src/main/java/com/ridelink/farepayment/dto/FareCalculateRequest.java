package com.ridelink.farepayment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FareCalculateRequest {
    @NotNull(message = "Ride ID is required")
    private Long rideId;
    
    private String pickupLocation;
    private String destinationLocation;
}
