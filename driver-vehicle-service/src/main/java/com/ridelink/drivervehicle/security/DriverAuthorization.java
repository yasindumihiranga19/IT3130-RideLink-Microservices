package com.ridelink.drivervehicle.security;

import com.ridelink.drivervehicle.repository.DriverRepository;
import org.springframework.stereotype.Component;

@Component("driverAuthorization")
public class DriverAuthorization {

    private final DriverRepository driverRepository;

    public DriverAuthorization(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    public boolean isOwnerOrMissing(Long driverId, String email) {
        return driverRepository.findById(driverId)
                .map(driver -> driver.getEmail() != null
                        && driver.getEmail().equalsIgnoreCase(email))
                .orElse(true);
    }
}