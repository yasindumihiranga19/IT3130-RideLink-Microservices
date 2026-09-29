package com.ridelink.ridemanagement.service;

import com.ridelink.ridemanagement.dto.DriverAssignmentDto;
import com.ridelink.ridemanagement.dto.RideRequestDto;
import com.ridelink.ridemanagement.dto.RideResponseDto;
import com.ridelink.ridemanagement.dto.StatusUpdateDto;
import com.ridelink.ridemanagement.exception.InvalidRideOperationException;
import com.ridelink.ridemanagement.exception.RideNotFoundException;
import com.ridelink.ridemanagement.model.Ride;
import com.ridelink.ridemanagement.model.RideStatus;
import com.ridelink.ridemanagement.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RideServiceImpl implements RideService {

    private final RideRepository rideRepository;
    private final FareServiceClient fareServiceClient;

    @Override
    @Transactional
    public RideResponseDto createRideRequest(RideRequestDto requestDto) {
        log.info("Creating ride request for passenger: {}", requestDto.getPassengerId());
        
        Double estimatedFare = fareServiceClient.getEstimatedFare(requestDto.getPickupLocation(), requestDto.getDestination());

        Ride ride = Ride.builder()
                .passengerId(requestDto.getPassengerId())
                .pickupLocation(requestDto.getPickupLocation())
                .destination(requestDto.getDestination())
                .status(RideStatus.REQUESTED)
                .fare(estimatedFare)
                .build();

        Ride savedRide = rideRepository.save(ride);
        return mapToResponseDto(savedRide);
    }

    @Override
    @Transactional
    public RideResponseDto assignDriver(Long rideId, DriverAssignmentDto assignmentDto) {
        log.info("Assigning driver {} to ride {}", assignmentDto.getDriverId(), rideId);
        
        Ride ride = getRideEntityById(rideId);
        
        if (ride.getStatus() != RideStatus.REQUESTED) {
            throw new InvalidRideOperationException("Driver can only be assigned to a REQUESTED ride. Current status: " + ride.getStatus());
        }

        ride.setDriverId(assignmentDto.getDriverId());
        ride.setStatus(RideStatus.ASSIGNED);
        
        return mapToResponseDto(rideRepository.save(ride));
    }

    @Override
    @Transactional
    public RideResponseDto updateRideStatus(Long rideId, StatusUpdateDto statusUpdateDto) {
        log.info("Updating status of ride {} to {}", rideId, statusUpdateDto.getStatus());
        
        Ride ride = getRideEntityById(rideId);
        RideStatus newStatus = statusUpdateDto.getStatus();
        RideStatus currentStatus = ride.getStatus();

        validateStatusTransition(currentStatus, newStatus);
        
        ride.setStatus(newStatus);
        return mapToResponseDto(rideRepository.save(ride));
    }

    @Override
    public RideResponseDto getRideById(Long rideId) {
        return mapToResponseDto(getRideEntityById(rideId));
    }

    @Override
    public List<RideResponseDto> getRidesByPassengerId(Long passengerId) {
        return rideRepository.findByPassengerId(passengerId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RideResponseDto> getRidesByDriverId(Long driverId) {
        return rideRepository.findByDriverId(driverId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancelRide(Long rideId) {
        Ride ride = getRideEntityById(rideId);
        if (ride.getStatus() == RideStatus.COMPLETED || ride.getStatus() == RideStatus.CANCELLED) {
            throw new InvalidRideOperationException("Cannot cancel a ride that is already " + ride.getStatus());
        }
        ride.setStatus(RideStatus.CANCELLED);
        rideRepository.save(ride);
    }

    private Ride getRideEntityById(Long rideId) {
        return rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("Ride not found with ID: " + rideId));
    }

    private void validateStatusTransition(RideStatus currentStatus, RideStatus newStatus) {
        boolean isValid = false;
        switch (currentStatus) {
            case REQUESTED:
                isValid = (newStatus == RideStatus.ASSIGNED || newStatus == RideStatus.CANCELLED);
                break;
            case ASSIGNED:
                isValid = (newStatus == RideStatus.ACCEPTED || newStatus == RideStatus.CANCELLED);
                break;
            case ACCEPTED:
                isValid = (newStatus == RideStatus.IN_PROGRESS || newStatus == RideStatus.CANCELLED);
                break;
            case IN_PROGRESS:
                isValid = (newStatus == RideStatus.COMPLETED);
                break;
            case COMPLETED:
            case CANCELLED:
                isValid = false;
                break;
        }

        if (!isValid) {
            throw new InvalidRideOperationException(
                    String.format("Invalid status transition from %s to %s", currentStatus, newStatus));
        }
    }

    private RideResponseDto mapToResponseDto(Ride ride) {
        return RideResponseDto.builder()
                .id(ride.getId())
                .passengerId(ride.getPassengerId())
                .driverId(ride.getDriverId())
                .pickupLocation(ride.getPickupLocation())
                .destination(ride.getDestination())
                .status(ride.getStatus())
                .fare(ride.getFare())
                .createdAt(ride.getCreatedAt())
                .updatedAt(ride.getUpdatedAt())
                .build();
    }
}
