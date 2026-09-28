package com.ridelink.drivervehicle.controller;

import com.ridelink.drivervehicle.model.AvailabilityStatus;
import com.ridelink.drivervehicle.model.Driver;
import com.ridelink.drivervehicle.model.Vehicle;
import com.ridelink.drivervehicle.exception.ConflictException;
import com.ridelink.drivervehicle.repository.DriverRepository;
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
    public ResponseEntity<Driver> getDriverById(@PathVariable Long id) {
        Optional<Driver> driver = driverRepository.findById(id);
        return driver.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // 4. Update availability status
    @PatchMapping("/{id}/availability")
    public ResponseEntity<Driver> updateAvailability(
            @PathVariable Long id,
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
            @PathVariable Long id,
            @RequestBody Map<String, Double> location) {

        Optional<Driver> optionalDriver = driverRepository.findById(id);
        if (optionalDriver.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Driver driver = optionalDriver.get();
        driver.setCurrentLatitude(location.get("latitude"));
        driver.setCurrentLongitude(location.get("longitude"));

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
     // 7. Add vehicle for a driver
    @PostMapping("/{id}/vehicle")
    public ResponseEntity<Driver> addVehicle(
            @PathVariable Long id,
            @Valid @RequestBody Vehicle vehicle) {

        Optional<Driver> optionalDriver = driverRepository.findById(id);
        if (optionalDriver.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Driver driver = optionalDriver.get();
        if (driver.getVehicle() != null) {
            throw new ConflictException("Driver " + id + " already has a vehicle assigned");
        }

        vehicle.setDriver(driver);
        driver.setVehicle(vehicle);

        Driver saved = driverRepository.save(driver);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}