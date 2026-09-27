package com.ridelink.farepayment.dto;

import lombok.Data;

@Data
public class FareEstimateRequest {
    private String pickupLocation;
    private String dropoffLocation;
    // You could also use coordinates here if preferred
    private double simulatedDistanceInKm;
}
