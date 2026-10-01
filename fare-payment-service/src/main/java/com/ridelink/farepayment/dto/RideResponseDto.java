package com.ridelink.farepayment.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RideResponseDto {
    private Long id;
    private Long passengerId;
    private Long driverId;
    private String pickupLocation;
    private String destinationLocation;
    private String status;
    private BigDecimal estimatedFare;
    private BigDecimal finalFare;
}
