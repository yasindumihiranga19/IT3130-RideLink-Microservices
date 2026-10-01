package com.ridelink.ridemanagement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CancelRideRequest {
    @NotBlank(message = "Cancellation reason is required")
    private String reason;
}
