package com.ridelink.ridemanagement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DriverAssignmentDto {
    @NotNull(message = "Driver ID is required")
    private Long driverId;
}
