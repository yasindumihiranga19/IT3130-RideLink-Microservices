package com.ridelink.driver_vehicle_service.controller;

import com.ridelink.driver_vehicle_service.model.AvailabilityStatus;
import com.ridelink.driver_vehicle_service.model.Driver;
import com.ridelink.driver_vehicle_service.model.Location;
import com.ridelink.driver_vehicle_service.repository.DriverRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    private final DriverRepository driverRepository;

    public DriverController(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    // 1. Register a new driver
    @PostMapping
    public ResponseEntity<Driver> registerDriver(@Valid @RequestBody Driver driver) {
        Driver saved = driverRepository.save(driver);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // 2. Get all drivers
    @GetMapping
    public ResponseEntity<List<Driver>> getAllDrivers() {
        return ResponseEntity.ok(driverRepository.findAll());
    }

    // 3. Get a driver by id
    @GetMapping("/{id}")
    public ResponseEntity<Driver> getDriverById(@PathVariable String id) {
        Optional<Driver> driver = driverRepository.findById(id);
        return driver.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // 4. Update availability status
    @PatchMapping("/{id}/availability")
    public ResponseEntity<Driver> updateAvailability(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {

        Optional<Driver> optionalDriver = driverRepository.findById(id);
        if (optionalDriver.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Driver driver = optionalDriver.get();
        try {
            AvailabilityStatus status = AvailabilityStatus.valueOf(body.get("availabilityStatus"));
            driver.setAvailabilityStatus(status);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(driverRepository.save(driver));
    }

    // 5. Update simulated current location
    @PatchMapping("/{id}/location")
    public ResponseEntity<Driver> updateLocation(
            @PathVariable String id,
            @RequestBody Location location) {

        Optional<Driver> optionalDriver = driverRepository.findById(id);
        if (optionalDriver.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Driver driver = optionalDriver.get();
        driver.setCurrentLocation(location);

        return ResponseEntity.ok(driverRepository.save(driver));
    }

    // 6. Get eligible available drivers (optionally filter by service area)
    @GetMapping("/available")
    public ResponseEntity<List<Driver>> getAvailableDrivers(
            @RequestParam(required = false) String serviceArea) {

        List<Driver> drivers;
        if (serviceArea != null && !serviceArea.isBlank()) {
            drivers = driverRepository.findByServiceAreaAndAvailabilityStatus(
                    serviceArea, AvailabilityStatus.AVAILABLE);
        } else {
            drivers = driverRepository.findByAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        }
        return ResponseEntity.ok(drivers);
    }
}