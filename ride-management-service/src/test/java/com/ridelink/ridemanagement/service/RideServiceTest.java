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
import com.ridelink.ridemanagement.exception.UnauthorizedRideOperationException;
import com.ridelink.ridemanagement.repository.RideRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideServiceTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private DriverClient driverClient;

    @Mock
    private FareClient fareClient;

    @InjectMocks
    private RideService rideService;

    private Ride ride;

    @BeforeEach
    void setUp() {
        ride = new Ride();
        ride.setId(1L);
        ride.setPassengerId(10L);
        ride.setPickupLocation("A");
        ride.setDestinationLocation("B");
        ride.setStatus(RideStatus.REQUESTED);
    }

    @Test
    void createRide_Success() {
        RideRequest req = new RideRequest();
        req.setPickupLocation("A");
        req.setDestinationLocation("B");

        when(rideRepository.save(any(Ride.class))).thenReturn(ride);

        RideResponse res = rideService.createRide(10L, req, "token");

        assertNotNull(res);
        assertEquals(RideStatus.REQUESTED, res.getStatus());
        verify(rideRepository).save(any(Ride.class));
    }

    @Test
    void assignDriver_Success() {
        DriverDTO driver = new DriverDTO();
        driver.setId(20L);

        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));
        when(driverClient.getAvailableDrivers("token")).thenReturn(List.of(driver));
        when(rideRepository.save(any(Ride.class))).thenReturn(ride);

        RideResponse res = rideService.assignDriver(1L, 10L, "PASSENGER", "token");

        assertEquals(RideStatus.ASSIGNED, res.getStatus());
        assertEquals(20L, res.getDriverId());
    }

    @Test
    void assignDriver_NoDriverAvailable() {
        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));
        when(driverClient.getAvailableDrivers("token")).thenReturn(Collections.emptyList());

        assertThrows(DriverNotAvailableException.class, () -> rideService.assignDriver(1L, 10L, "PASSENGER", "token"));
    }

    @Test
    void acceptRide_Success() {
        ride.setStatus(RideStatus.ASSIGNED);
        ride.setDriverId(20L);

        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenReturn(ride);

        RideResponse res = rideService.acceptRide(1L, 20L);

        assertEquals(RideStatus.ACCEPTED, res.getStatus());
    }

    @Test
    void acceptRide_Unauthorized() {
        ride.setStatus(RideStatus.ASSIGNED);
        ride.setDriverId(20L);

        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));

        assertThrows(UnauthorizedRideOperationException.class, () -> rideService.acceptRide(1L, 99L));
    }

    @Test
    void startRide_Success() {
        ride.setStatus(RideStatus.ACCEPTED);
        ride.setDriverId(20L);

        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenReturn(ride);

        RideResponse res = rideService.startRide(1L, 20L);

        assertEquals(RideStatus.IN_PROGRESS, res.getStatus());
    }

    @Test
    void completeRide_Success() {
        ride.setStatus(RideStatus.IN_PROGRESS);
        ride.setDriverId(20L);

        FareResponseDTO fareRes = new FareResponseDTO();
        fareRes.setFinalFare(new BigDecimal("15.50"));

        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));
        when(fareClient.calculateFare(1L, "A", "B", "token")).thenReturn(fareRes);
        when(rideRepository.save(any(Ride.class))).thenReturn(ride);

        RideResponse res = rideService.completeRide(1L, 20L, "token");

        assertEquals(RideStatus.COMPLETED, res.getStatus());
        assertEquals(new BigDecimal("15.50"), res.getFinalFare());
    }

    @Test
    void completeRide_InvalidStatus() {
        ride.setStatus(RideStatus.REQUESTED);
        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));

        assertThrows(InvalidRideStatusException.class, () -> rideService.completeRide(1L, 20L, "token"));
    }

    @Test
    void cancelRide_Success_Passenger() {
        CancelRideRequest req = new CancelRideRequest();
        req.setReason("Changed mind");

        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenReturn(ride);

        RideResponse res = rideService.cancelRide(1L, 10L, false, req, "token");

        assertEquals(RideStatus.CANCELLED, res.getStatus());
        assertEquals("Changed mind", res.getCancellationReason());
    }

    @Test
    void cancelRide_AfterCompletion_Fails() {
        ride.setStatus(RideStatus.COMPLETED);
        CancelRideRequest req = new CancelRideRequest();

        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));

        assertThrows(InvalidRideStatusException.class, () -> rideService.cancelRide(1L, 10L, false, req, "token"));
    }
}
