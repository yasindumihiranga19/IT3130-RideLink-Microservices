package com.ridelink.ridemanagement.controller;

import com.ridelink.ridemanagement.client.AccountClient;
import com.ridelink.ridemanagement.dto.AccountDTO;
import com.ridelink.ridemanagement.dto.CancelRideRequest;
import com.ridelink.ridemanagement.dto.RideRequest;
import com.ridelink.ridemanagement.dto.RideResponse;
import com.ridelink.ridemanagement.service.RideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rides")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Ride Management", description = "Endpoints for managing rides")
public class RideController {

    private final RideService rideService;
    private final AccountClient accountClient;

    public RideController(RideService rideService, AccountClient accountClient) {
        this.rideService = rideService;
        this.accountClient = accountClient;
    }

    @PostMapping
    @PreAuthorize("hasRole('PASSENGER')")
    @Operation(summary = "Create a new ride request")
    public ResponseEntity<RideResponse> createRide(@Valid @RequestBody RideRequest request,
                                                   @RequestHeader("Authorization") String token) {
        AccountDTO account = accountClient.getCurrentAccount(token);
        RideResponse response = rideService.createRide(account.getId(), request, token);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('PASSENGER', 'ADMIN')")
    @Operation(summary = "Assign a driver to a requested ride")
    public ResponseEntity<RideResponse> assignDriver(@PathVariable Long id,
                                                     @RequestHeader("Authorization") String token) {
        AccountDTO account = accountClient.getCurrentAccount(token);
        RideResponse response = rideService.assignDriver(id, account.getId(), account.getRole(), token);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/accept")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Accept an assigned ride")
    public ResponseEntity<RideResponse> acceptRide(@PathVariable Long id,
                                                   @RequestHeader("Authorization") String token) {
        AccountDTO driver = accountClient.getCurrentAccount(token);
        RideResponse response = rideService.acceptRide(id, driver.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/start")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Start an accepted ride")
    public ResponseEntity<RideResponse> startRide(@PathVariable Long id,
                                                  @RequestHeader("Authorization") String token) {
        AccountDTO driver = accountClient.getCurrentAccount(token);
        RideResponse response = rideService.startRide(id, driver.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Complete an in-progress ride")
    public ResponseEntity<RideResponse> completeRide(@PathVariable Long id,
                                                     @RequestHeader("Authorization") String token) {
        AccountDTO driver = accountClient.getCurrentAccount(token);
        RideResponse response = rideService.completeRide(id, driver.getId(), token);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('PASSENGER', 'DRIVER')")
    @Operation(summary = "Cancel a ride")
    public ResponseEntity<RideResponse> cancelRide(@PathVariable Long id,
                                                   @Valid @RequestBody CancelRideRequest request,
                                                   @RequestHeader("Authorization") String token) {
        AccountDTO user = accountClient.getCurrentAccount(token);
        boolean isDriver = "DRIVER".equalsIgnoreCase(user.getRole());
        RideResponse response = rideService.cancelRide(id, user.getId(), isDriver, request, token);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PASSENGER', 'DRIVER', 'ADMIN')")
    @Operation(summary = "Get a ride by ID")
    public ResponseEntity<RideResponse> getRideById(@PathVariable Long id,
                                                    @RequestHeader("Authorization") String token) {
        RideResponse ride = rideService.getRideResponseById(id);
        AccountDTO account = accountClient.getCurrentAccount(token);
        
        if (!"ADMIN".equalsIgnoreCase(account.getRole())) {
            if ("PASSENGER".equalsIgnoreCase(account.getRole()) && !ride.getPassengerId().equals(account.getId())) {
                throw new com.ridelink.ridemanagement.exception.UnauthorizedRideOperationException("You can only view your own rides.");
            }
            // if ("DRIVER".equalsIgnoreCase(account.getRole()) && (ride.getDriverId() == null || !ride.getDriverId().equals(account.getId()))) {
            //     throw new com.ridelink.ridemanagement.exception.UnauthorizedRideOperationException("You can only view rides assigned to you.");
            // }
        }
        
        return ResponseEntity.ok(ride);
    }

    @GetMapping("/passenger/{passengerId}")
    @PreAuthorize("hasAnyRole('PASSENGER', 'ADMIN')")
    @Operation(summary = "Get rides by passenger ID")
    public ResponseEntity<List<RideResponse>> getRidesByPassenger(@PathVariable Long passengerId,
                                                                  @RequestHeader("Authorization") String token) {
        AccountDTO account = accountClient.getCurrentAccount(token);
        if ("PASSENGER".equalsIgnoreCase(account.getRole()) && !account.getId().equals(passengerId)) {
            throw new com.ridelink.ridemanagement.exception.UnauthorizedRideOperationException("You can only view your own rides.");
        }
        return ResponseEntity.ok(rideService.getRidesByPassenger(passengerId));
    }

    @GetMapping("/driver/{driverId}")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
    @Operation(summary = "Get rides by driver ID")
    public ResponseEntity<List<RideResponse>> getRidesByDriver(@PathVariable Long driverId,
                                                               @RequestHeader("Authorization") String token) {
        AccountDTO account = accountClient.getCurrentAccount(token);
        // if ("DRIVER".equalsIgnoreCase(account.getRole()) && !account.getId().equals(driverId)) {
        //     throw new com.ridelink.ridemanagement.exception.UnauthorizedRideOperationException("You can only view rides assigned to you.");
        // }
        return ResponseEntity.ok(rideService.getRidesByDriver(driverId));
    }
}
