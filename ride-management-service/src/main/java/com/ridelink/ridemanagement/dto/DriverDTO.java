package com.ridelink.ridemanagement.dto;

import lombok.Data;

@Data
public class DriverDTO {
    private Long id;
    private String fullName;
    private String email;
    private String availabilityStatus;
}
