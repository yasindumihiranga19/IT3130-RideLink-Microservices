package com.ridelink.ridemanagement.controller;

import com.ridelink.ridemanagement.config.JwtUtil;
import com.ridelink.ridemanagement.dto.DriverAssignmentDto;
import com.ridelink.ridemanagement.dto.RideRequestDto;
import com.ridelink.ridemanagement.dto.RideResponseDto;
import com.ridelink.ridemanagement.dto.StatusUpdateDto;
import com.ridelink.ridemanagement.service.RideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/rides")
@RequiredArgsConstructor
@Tag(name = "Ride Management", description = "Endpoints for managing ride lifecycle")
public class RideController {

    private final RideService rideService;
    private final JwtUtil jwtUtil; // Added for demo token

    @GetMapping("/demo-token")
    @Operation(summary = "Generate a Demo JWT Token", description = "Temporary endpoint to generate a token for Swagger testing since Account Service is down")
    public ResponseEntity<Map<String, String>> generateDemoToken(
            @RequestParam(defaultValue = "100") String userId,
            @RequestParam(defaultValue = "ROLE_PASSENGER") String role) {
        String token = jwtUtil.generateToken(userId, role);
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Create a new ride request", description = "Passenger requests a new ride from pickup to destination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Ride requested successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<RideResponseDto> createRideRequest(@Valid @RequestBody RideRequestDto requestDto) {
        RideResponseDto response = rideService.createRideRequest(requestDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ride by ID", description = "Retrieve details of a specific ride")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation"),
        @ApiResponse(responseCode = "404", description = "Ride not found")
    })
    public ResponseEntity<RideResponseDto> getRideById(@PathVariable Long id) {
        return ResponseEntity.ok(rideService.getRideById(id));
    }

    @GetMapping("/passenger/{passengerId}")
    @Operation(summary = "Get all rides for a passenger", description = "Retrieve a list of rides associated with a specific passenger")
    public ResponseEntity<List<RideResponseDto>> getRidesByPassengerId(@PathVariable Long passengerId) {
        return ResponseEntity.ok(rideService.getRidesByPassengerId(passengerId));
    }

    @GetMapping("/driver/{driverId}")
    @Operation(summary = "Get all rides for a driver", description = "Retrieve a list of rides assigned to a specific driver")
    public ResponseEntity<List<RideResponseDto>> getRidesByDriverId(@PathVariable Long driverId) {
        return ResponseEntity.ok(rideService.getRidesByDriverId(driverId));
    }

    @PutMapping("/{id}/assign-driver")
    @Operation(summary = "Assign a driver to a ride", description = "Updates a ride with REQUESTED status to ASSIGNED by setting the driver ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Driver assigned successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid status transition or input"),
        @ApiResponse(responseCode = "404", description = "Ride not found")
    })
    public ResponseEntity<RideResponseDto> assignDriver(@PathVariable Long id, 
                                                       @Valid @RequestBody DriverAssignmentDto assignmentDto) {
        return ResponseEntity.ok(rideService.assignDriver(id, assignmentDto));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update ride status", description = "Update the status of a ride (e.g. ACCEPTED, IN_PROGRESS, COMPLETED)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid status transition"),
        @ApiResponse(responseCode = "404", description = "Ride not found")
    })
    public ResponseEntity<RideResponseDto> updateRideStatus(@PathVariable Long id, 
                                                           @Valid @RequestBody StatusUpdateDto statusUpdateDto) {
        return ResponseEntity.ok(rideService.updateRideStatus(id, statusUpdateDto));
    }

    @DeleteMapping("/{id}/cancel")
    @Operation(summary = "Cancel a ride", description = "Cancels a ride if it is not already completed or cancelled")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Ride cancelled successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot cancel completed/cancelled ride"),
        @ApiResponse(responseCode = "404", description = "Ride not found")
    })
    public ResponseEntity<Void> cancelRide(@PathVariable Long id) {
        rideService.cancelRide(id);
        return ResponseEntity.noContent().build();
    }
}
