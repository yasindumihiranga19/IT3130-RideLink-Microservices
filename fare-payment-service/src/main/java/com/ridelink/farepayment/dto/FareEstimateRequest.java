package com.ridelink.farepayment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class FareEstimateRequest {
    @NotBlank(message = "Pickup location is required")
    private String pickupLocation;
    
    @NotBlank(message = "Destination location is required")
    private String destinationLocation;
    
    // You could also use coordinates here if preferred
    @PositiveOrZero(message = "Distance must be zero or positive")
    private double simulatedDistanceInKm;
}
