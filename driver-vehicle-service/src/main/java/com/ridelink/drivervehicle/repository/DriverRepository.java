package com.ridelink.drivervehicle.repository;

import com.ridelink.drivervehicle.model.AvailabilityStatus;
import com.ridelink.drivervehicle.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DriverRepository extends JpaRepository<Driver, Long> {

    List<Driver> findByAvailabilityStatus(AvailabilityStatus status);

    List<Driver> findByServiceAreaIgnoreCaseAndAvailabilityStatus(String serviceArea, AvailabilityStatus status);
}