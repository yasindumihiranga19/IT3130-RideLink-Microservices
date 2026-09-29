package com.ridelink.drivervehicle.controller;

import com.ridelink.drivervehicle.dto.DriverProfileUpdateRequest;
import com.ridelink.drivervehicle.model.AvailabilityStatus;
import com.ridelink.drivervehicle.model.Driver;
import com.ridelink.drivervehicle.model.Vehicle;
import com.ridelink.drivervehicle.exception.ConflictException;
import com.ridelink.drivervehicle.repository.DriverRepository;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/drivers")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Drivers and vehicles", description = "Driver profiles, availability, service areas, and assigned vehicles")
public class DriverController {

    private final DriverRepository driverRepository;

    public DriverController(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    // 1. Register a new driver
    @PostMapping
        @Operation(summary = "Register a driver", description = "Creates a driver profile. A driver may register only an account with a matching email; admins may register any driver.")
        @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Driver profile created"),
            @ApiResponse(responseCode = "400", description = "Invalid driver details"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token"),
            @ApiResponse(responseCode = "403", description = "Caller cannot register this driver"),
            @ApiResponse(responseCode = "409", description = "Email or license number is already registered")
        })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DRIVER') and authentication.name.equalsIgnoreCase(#driver.email))")
    public ResponseEntity<Driver> registerDriver(@Valid @RequestBody Driver driver) {
        driver.setServiceArea(normalizeOptionalServiceArea(driver.getServiceArea()));
        Driver saved = driverRepository.save(driver);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // 2. Get all drivers
    @GetMapping
        @Operation(summary = "List all drivers", description = "Returns all driver profiles. Restricted to administrators.")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Driver profiles returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token"),
            @ApiResponse(responseCode = "403", description = "Administrator role required")
        })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Driver>> getAllDrivers() {
        return ResponseEntity.ok(driverRepository.findAll());
    }

    // 3. Get a driver by id
    @GetMapping("/{id}")
        @Operation(summary = "Get a driver", description = "Returns one driver by ID. Drivers may view only their own profile; passengers and admins may view any profile.")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Driver profile returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token"),
            @ApiResponse(responseCode = "403", description = "Caller cannot view this driver"),
            @ApiResponse(responseCode = "404", description = "Driver not found")
        })
    @PreAuthorize("hasAnyRole('PASSENGER', 'ADMIN') or (hasRole('DRIVER') and @driverAuthorization.isOwnerOrMissing(#id, authentication.name))")
    public ResponseEntity<Driver> getDriverById(@PathVariable Long id) {
        Optional<Driver> driver = driverRepository.findById(id);
        return driver.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping("/{id}/profile")
        @Operation(summary = "Update driver profile", description = "Updates the driver's name, phone, license number, and optional service area. Email, availability, location, and vehicle assignment are not changed.")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Driver profile updated"),
            @ApiResponse(responseCode = "400", description = "Required profile field is missing or blank"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token"),
            @ApiResponse(responseCode = "403", description = "Caller cannot update this driver"),
            @ApiResponse(responseCode = "404", description = "Driver not found")
        })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DRIVER') and @driverAuthorization.isOwnerOrMissing(#id, authentication.name))")
    public ResponseEntity<Driver> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody DriverProfileUpdateRequest request) {

        Optional<Driver> optionalDriver = driverRepository.findById(id);
        if (optionalDriver.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Driver driver = optionalDriver.get();
        driver.setFullName(request.fullName().trim());
        driver.setPhoneNumber(request.phoneNumber().trim());
        driver.setLicenseNumber(request.licenseNumber().trim());
        driver.setServiceArea(normalizeOptionalServiceArea(request.serviceArea()));

        return ResponseEntity.ok(driverRepository.save(driver));
    }

    // 4. Update availability status
    @PatchMapping("/{id}/availability")
        @Operation(summary = "Update driver availability", description = "Changes a driver's availability status. Only the driver or an administrator may update it.")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Availability updated"),
            @ApiResponse(responseCode = "400", description = "Unknown availability status"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token"),
            @ApiResponse(responseCode = "403", description = "Caller cannot update this driver"),
            @ApiResponse(responseCode = "404", description = "Driver not found")
        })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DRIVER') and @driverAuthorization.isOwnerOrMissing(#id, authentication.name))")
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
        @Operation(summary = "Update driver location", description = "Updates the driver's current latitude and longitude. Only the driver or an administrator may update it.")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Location updated"),
            @ApiResponse(responseCode = "400", description = "Invalid location coordinates"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token"),
            @ApiResponse(responseCode = "403", description = "Caller cannot update this driver"),
            @ApiResponse(responseCode = "404", description = "Driver not found")
        })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DRIVER') and @driverAuthorization.isOwnerOrMissing(#id, authentication.name))")
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

    @PatchMapping("/{id}/service-area")
        @Operation(summary = "Update driver service area", description = "Sets the driver's service area. The value is trimmed before saving.")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Service area updated"),
            @ApiResponse(responseCode = "400", description = "Service area is missing or blank"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token"),
            @ApiResponse(responseCode = "403", description = "Caller cannot update this driver"),
            @ApiResponse(responseCode = "404", description = "Driver not found")
        })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DRIVER') and @driverAuthorization.isOwnerOrMissing(#id, authentication.name))")
    public ResponseEntity<Driver> updateServiceArea(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        Optional<Driver> optionalDriver = driverRepository.findById(id);
        if (optionalDriver.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String serviceArea = body.get("serviceArea");
        if (serviceArea == null || serviceArea.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Driver driver = optionalDriver.get();
        driver.setServiceArea(normalizeOptionalServiceArea(serviceArea));
        return ResponseEntity.ok(driverRepository.save(driver));
    }

    // 6. Get eligible available drivers (optionally filter by service area)
    @GetMapping("/available")
        @Operation(summary = "Find available drivers", description = "Returns available drivers, optionally filtered by service area. Service-area matching ignores case and surrounding spaces.")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Available drivers returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token")
        })
    @PreAuthorize("hasAnyRole('PASSENGER', 'DRIVER', 'ADMIN')")
    public ResponseEntity<List<Driver>> getAvailableDrivers(
            @RequestParam(required = false) String serviceArea) {

        List<Driver> drivers;
        if (serviceArea != null && !serviceArea.isBlank()) {
            drivers = driverRepository.findByServiceAreaIgnoreCaseAndAvailabilityStatus(
                normalizeOptionalServiceArea(serviceArea), AvailabilityStatus.AVAILABLE);
        } else {
            drivers = driverRepository.findByAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        }
        return ResponseEntity.ok(drivers);
    }
     // 7. Add vehicle for a driver
    @PostMapping("/{id}/vehicle")
        @Operation(summary = "Add a vehicle", description = "Assigns a vehicle to a driver who does not already have one.")
        @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Vehicle assigned"),
            @ApiResponse(responseCode = "400", description = "Invalid vehicle details"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token"),
            @ApiResponse(responseCode = "403", description = "Caller cannot update this driver"),
            @ApiResponse(responseCode = "404", description = "Driver not found"),
            @ApiResponse(responseCode = "409", description = "Driver already has a vehicle")
        })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DRIVER') and @driverAuthorization.isOwnerOrMissing(#id, authentication.name))")
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

    @GetMapping("/{id}/vehicle")
        @Operation(summary = "Get a driver's vehicle", description = "Returns the vehicle assigned to a driver.")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehicle returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token"),
            @ApiResponse(responseCode = "403", description = "Caller cannot view this driver's vehicle"),
            @ApiResponse(responseCode = "404", description = "Driver or assigned vehicle not found")
        })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DRIVER') and @driverAuthorization.isOwnerOrMissing(#id, authentication.name))")
    public ResponseEntity<Vehicle> getVehicle(@PathVariable Long id) {
        Optional<Driver> optionalDriver = driverRepository.findById(id);
        if (optionalDriver.isEmpty() || optionalDriver.get().getVehicle() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(optionalDriver.get().getVehicle());
    }

    @PutMapping("/{id}/vehicle")
        @Operation(summary = "Update a driver's vehicle", description = "Updates the details of the vehicle already assigned to a driver.")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehicle updated"),
            @ApiResponse(responseCode = "400", description = "Invalid vehicle details"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token"),
            @ApiResponse(responseCode = "403", description = "Caller cannot update this driver's vehicle"),
            @ApiResponse(responseCode = "404", description = "Driver or assigned vehicle not found")
        })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DRIVER') and @driverAuthorization.isOwnerOrMissing(#id, authentication.name))")
    public ResponseEntity<Vehicle> updateVehicle(
            @PathVariable Long id,
            @Valid @RequestBody Vehicle updatedVehicle) {

        Optional<Driver> optionalDriver = driverRepository.findById(id);
        if (optionalDriver.isEmpty() || optionalDriver.get().getVehicle() == null) {
            return ResponseEntity.notFound().build();
        }

        Vehicle vehicle = optionalDriver.get().getVehicle();
        vehicle.setVehicleNumber(updatedVehicle.getVehicleNumber());
        vehicle.setType(updatedVehicle.getType());
        vehicle.setModel(updatedVehicle.getModel());
        vehicle.setColor(updatedVehicle.getColor());
        vehicle.setCapacity(updatedVehicle.getCapacity());

        Driver saved = driverRepository.save(optionalDriver.get());
        return ResponseEntity.ok(saved.getVehicle());
    }

    private String normalizeOptionalServiceArea(String serviceArea) {
        if (serviceArea == null || serviceArea.isBlank()) {
            return null;
        }
        return serviceArea.trim();
    }
}