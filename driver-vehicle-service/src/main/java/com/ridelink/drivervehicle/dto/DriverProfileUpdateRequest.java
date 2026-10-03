package com.ridelink.drivervehicle.dto;

import jakarta.validation.constraints.NotBlank;

public record DriverProfileUpdateRequest(
        @NotBlank String fullName,
        @NotBlank String phoneNumber,
        @NotBlank String licenseNumber,
        String serviceArea) {
}
