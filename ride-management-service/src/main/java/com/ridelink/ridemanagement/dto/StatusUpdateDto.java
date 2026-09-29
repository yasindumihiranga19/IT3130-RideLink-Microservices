package com.ridelink.ridemanagement.dto;

import com.ridelink.ridemanagement.model.RideStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusUpdateDto {
    @NotNull(message = "Status is required")
    private RideStatus status;
}
