package com.ridelink.ridemanagement.service;

import com.ridelink.ridemanagement.client.DriverClient;
import com.ridelink.ridemanagement.client.FareClient;
import com.ridelink.ridemanagement.dto.CancelRideRequest;
import com.ridelink.ridemanagement.dto.DriverDTO;
import com.ridelink.ridemanagement.dto.FareResponseDTO;
import com.ridelink.ridemanagement.dto.RideRequest;
import com.ridelink.ridemanagement.dto.RideResponse;
import com.ridelink.ridemanagement.entity.Ride;
import com.ridelink.ridemanagement.enums.RideStatus;
import com.ridelink.ridemanagement.exception.DriverNotAvailableException;
import com.ridelink.ridemanagement.exception.InvalidRideStatusException;
import com.ridelink.ridemanagement.exception.RideNotFoundException;
import com.ridelink.ridemanagement.exception.UnauthorizedRideOperationException;
import com.ridelink.ridemanagement.repository.RideRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RideService {

    private final RideRepository rideRepository;
    private final DriverClient driverClient;
    private final FareClient fareClient;

    public RideService(RideRepository rideRepository, DriverClient driverClient, FareClient fareClient) {
        this.rideRepository = rideRepository;
        this.driverClient = driverClient;
        this.fareClient = fareClient;
    }

    @Transactional
    public RideResponse createRide(Long passengerId, RideRequest request, String token) {
        Ride ride = Ride.builder()
                .passengerId(passengerId)
                .pickupLocation(request.getPickupLocation())
                .destinationLocation(request.getDestinationLocation())
                .status(RideStatus.REQUESTED)
                .build();
        
        try {
            FareResponseDTO fareEstimate = fareClient.estimateFare(request.getPickupLocation(), request.getDestinationLocation(), token);
            if (fareEstimate != null && fareEstimate.getEstimatedFare() != null) {
                ride.setEstimatedFare(fareEstimate.getEstimatedFare());
            }
        } catch (Exception e) {
            // Log and ignore to allow ride creation even if fare estimate fails initially
            System.err.println("Failed to calculate estimated fare: " + e.getMessage());
        }
        
        Ride saved = rideRepository.save(ride);
        return mapToResponse(saved);
    }

    @Transactional
    public RideResponse assignDriver(Long rideId, Long userId, String role, String token) {
        Ride ride = getRideById(rideId);
        
        if (!"ADMIN".equalsIgnoreCase(role) && !ride.getPassengerId().equals(userId)) {
            throw new UnauthorizedRideOperationException("Only the requesting passenger can assign a driver to this ride.");
        }
        
        if (ride.getStatus() != RideStatus.REQUESTED) {
            throw new InvalidRideStatusException("Can only assign driver to a REQUESTED ride.");
        }

        List<DriverDTO> drivers = driverClient.getAvailableDrivers(token);
        if (drivers == null || drivers.isEmpty()) {
            throw new DriverNotAvailableException("No available drivers found at this time.");
        }

        // Simple assignment: Pick the first available driver
        DriverDTO assignedDriver = drivers.get(0);
        
        ride.setDriverId(assignedDriver.getId());
        ride.setStatus(RideStatus.ASSIGNED);
        ride.setAssignedAt(LocalDateTime.now());
        
        driverClient.updateAvailability(assignedDriver.getId(), "BUSY", token);
        
        Ride saved = rideRepository.save(ride);
        return mapToResponse(saved);
    }

    @Transactional
    public RideResponse acceptRide(Long rideId, Long driverId) {
        Ride ride = getRideById(rideId);
        
        if (ride.getStatus() != RideStatus.ASSIGNED) {
            throw new InvalidRideStatusException("Ride must be in ASSIGNED status to be accepted.");
        }
        
        // if (!ride.getDriverId().equals(driverId)) {
        //     throw new UnauthorizedRideOperationException("Only the assigned driver can accept this ride.");
        // }


        ride.setStatus(RideStatus.ACCEPTED);
        ride.setAcceptedAt(LocalDateTime.now());
        
        return mapToResponse(rideRepository.save(ride));
    }

    @Transactional
    public RideResponse startRide(Long rideId, Long driverId) {
        Ride ride = getRideById(rideId);
        
        if (ride.getStatus() != RideStatus.ACCEPTED) {
            throw new InvalidRideStatusException("Ride must be in ACCEPTED status to be started.");
        }
        
        // if (!ride.getDriverId().equals(driverId)) {
        //     throw new UnauthorizedRideOperationException("Only the assigned driver can start this ride.");
        // }


        ride.setStatus(RideStatus.IN_PROGRESS);
        ride.setStartedAt(LocalDateTime.now());
        
        return mapToResponse(rideRepository.save(ride));
    }

    @Transactional
    public RideResponse completeRide(Long rideId, Long driverId, String token) {
        Ride ride = getRideById(rideId);
        
        if (ride.getStatus() != RideStatus.IN_PROGRESS) {
            throw new InvalidRideStatusException("Ride must be in IN_PROGRESS status to be completed.");
        }
        
        // if (!ride.getDriverId().equals(driverId)) {
        //     throw new UnauthorizedRideOperationException("Only the assigned driver can complete this ride.");
        // }


        ride.setStatus(RideStatus.COMPLETED);
        ride.setCompletedAt(LocalDateTime.now());

        // Calculate final fare
        FareResponseDTO fareResponse = fareClient.calculateFare(ride.getId(), ride.getPickupLocation(), ride.getDestinationLocation(), token);
        if (fareResponse != null && fareResponse.getFinalFare() != null) {
            ride.setFinalFare(fareResponse.getFinalFare());
        }
        
        driverClient.updateAvailability(ride.getDriverId(), "AVAILABLE", token);
        
        return mapToResponse(rideRepository.save(ride));
    }

    @Transactional
    public RideResponse cancelRide(Long rideId, Long userId, boolean isDriver, CancelRideRequest request, String token) {
        Ride ride = getRideById(rideId);
        
        if (ride.getStatus() == RideStatus.COMPLETED) {
            throw new InvalidRideStatusException("Cannot cancel a COMPLETED ride.");
        }

        if (isDriver) {
            // if (ride.getDriverId() == null || !ride.getDriverId().equals(userId)) {
            //     throw new UnauthorizedRideOperationException("Only the assigned driver can cancel this ride.");
            // }

        } else {
            if (!ride.getPassengerId().equals(userId)) {
                throw new UnauthorizedRideOperationException("Only the requesting passenger can cancel this ride.");
            }
        }
        
        if (ride.getStatus() == RideStatus.CANCELLED) {
            return mapToResponse(ride); // idempotent
        }
        
        if (ride.getDriverId() != null) {
            driverClient.updateAvailability(ride.getDriverId(), "AVAILABLE", token);
        }

        ride.setStatus(RideStatus.CANCELLED);
        ride.setCancelledAt(LocalDateTime.now());
        ride.setCancellationReason(request.getReason());
        
        return mapToResponse(rideRepository.save(ride));
    }

    public RideResponse getRideResponseById(Long id) {
        return mapToResponse(getRideById(id));
    }
    
    public List<RideResponse> getRidesByPassenger(Long passengerId) {
        return rideRepository.findByPassengerId(passengerId).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<RideResponse> getRidesByDriver(Long driverId) {
        return rideRepository.findByDriverId(driverId).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private Ride getRideById(Long id) {
        return rideRepository.findById(id).orElseThrow(() -> new RideNotFoundException("Ride not found with id: " + id));
    }

    private RideResponse mapToResponse(Ride ride) {
        return RideResponse.builder()
                .id(ride.getId())
                .passengerId(ride.getPassengerId())
                .driverId(ride.getDriverId())
                .pickupLocation(ride.getPickupLocation())
                .destinationLocation(ride.getDestinationLocation())
                .status(ride.getStatus())
                .estimatedFare(ride.getEstimatedFare())
                .finalFare(ride.getFinalFare())
                .createdAt(ride.getCreatedAt())
                .assignedAt(ride.getAssignedAt())
                .acceptedAt(ride.getAcceptedAt())
                .startedAt(ride.getStartedAt())
                .completedAt(ride.getCompletedAt())
                .cancelledAt(ride.getCancelledAt())
                .cancellationReason(ride.getCancellationReason())
                .build();
    }
}
