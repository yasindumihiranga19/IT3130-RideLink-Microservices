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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideServiceImplTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private FareServiceClient fareServiceClient;

    @InjectMocks
    private RideServiceImpl rideService;

    private Ride ride;
    private RideRequestDto requestDto;

    @BeforeEach
    void setUp() {
        ride = Ride.builder()
                .id(1L)
                .passengerId(100L)
                .pickupLocation("Downtown")
                .destination("Airport")
                .status(RideStatus.REQUESTED)
                .fare(15.50)
                .createdAt(LocalDateTime.now())
                .build();

        requestDto = new RideRequestDto();
        requestDto.setPassengerId(100L);
        requestDto.setPickupLocation("Downtown");
        requestDto.setDestination("Airport");
    }

    @Test
    void createRideRequest_Success() {
        when(fareServiceClient.getEstimatedFare(anyString(), anyString())).thenReturn(25.50);
        when(rideRepository.save(any(Ride.class))).thenReturn(ride);

        RideResponseDto response = rideService.createRideRequest(requestDto);

        assertNotNull(response);
        assertEquals(100L, response.getPassengerId());
        assertEquals(RideStatus.REQUESTED, response.getStatus());
        verify(rideRepository, times(1)).save(any(Ride.class));
        verify(fareServiceClient, times(1)).getEstimatedFare(anyString(), anyString());
    }

    @Test
    void assignDriver_Success() {
        ride.setStatus(RideStatus.REQUESTED);
        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenReturn(ride);

        DriverAssignmentDto assignmentDto = new DriverAssignmentDto();
        assignmentDto.setDriverId(200L);

        RideResponseDto response = rideService.assignDriver(1L, assignmentDto);

        assertNotNull(response);
        assertEquals(200L, response.getDriverId());
        assertEquals(RideStatus.ASSIGNED, response.getStatus());
    }

    @Test
    void assignDriver_Fail_InvalidStatus() {
        ride.setStatus(RideStatus.IN_PROGRESS);
        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));

        DriverAssignmentDto assignmentDto = new DriverAssignmentDto();
        assignmentDto.setDriverId(200L);

        assertThrows(InvalidRideOperationException.class, () -> {
            rideService.assignDriver(1L, assignmentDto);
        });
        verify(rideRepository, never()).save(any(Ride.class));
    }

    @Test
    void updateRideStatus_Success() {
        ride.setStatus(RideStatus.ASSIGNED);
        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenReturn(ride);

        StatusUpdateDto updateDto = new StatusUpdateDto();
        updateDto.setStatus(RideStatus.ACCEPTED);

        RideResponseDto response = rideService.updateRideStatus(1L, updateDto);

        assertNotNull(response);
        assertEquals(RideStatus.ACCEPTED, response.getStatus());
    }

    @Test
    void updateRideStatus_Fail_InvalidTransition() {
        ride.setStatus(RideStatus.REQUESTED);
        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));

        StatusUpdateDto updateDto = new StatusUpdateDto();
        // Cannot go from REQUESTED straight to IN_PROGRESS
        updateDto.setStatus(RideStatus.IN_PROGRESS); 

        assertThrows(InvalidRideOperationException.class, () -> {
            rideService.updateRideStatus(1L, updateDto);
        });
    }

    @Test
    void getRideById_NotFound() {
        when(rideRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RideNotFoundException.class, () -> {
            rideService.getRideById(99L);
        });
    }
}
