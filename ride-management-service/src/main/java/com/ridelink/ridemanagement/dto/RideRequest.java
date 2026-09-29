package com.ridelink.ridemanagement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RideRequest {

    @NotBlank(message = "Pickup location is required")
    private String pickupLocation;

    @NotBlank(message = "Destination location is required")
    private String destinationLocation;
}
