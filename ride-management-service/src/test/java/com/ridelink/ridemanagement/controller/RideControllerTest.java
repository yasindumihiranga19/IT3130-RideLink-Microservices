package com.ridelink.ridemanagement.controller;

import com.ridelink.ridemanagement.client.AccountClient;
import com.ridelink.ridemanagement.dto.AccountDTO;
import com.ridelink.ridemanagement.dto.RideResponse;
import com.ridelink.ridemanagement.exception.UnauthorizedRideOperationException;
import com.ridelink.ridemanagement.service.RideService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RideControllerTest {

    @Mock
    private RideService rideService;

    @Mock
    private AccountClient accountClient;

    @InjectMocks
    private RideController rideController;

    private AccountDTO passengerAccount;
    private AccountDTO driverAccount;
    private RideResponse rideResponse;

    @BeforeEach
    void setUp() {
        passengerAccount = new AccountDTO();
        passengerAccount.setId(1L);
        passengerAccount.setRole("PASSENGER");

        driverAccount = new AccountDTO();
        driverAccount.setId(2L);
        driverAccount.setRole("DRIVER");

        rideResponse = RideResponse.builder()
                .id(100L)
                .passengerId(1L)
                .driverId(2L)
                .build();
    }

    @Test
    void getRideById_PassengerCanViewOwnRide() {
        String token = "Bearer token1";
        when(rideService.getRideResponseById(100L)).thenReturn(rideResponse);
        when(accountClient.getCurrentAccount(token)).thenReturn(passengerAccount);

        ResponseEntity<RideResponse> response = rideController.getRideById(100L, token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(100L, response.getBody().getId());
    }

    @Test
    void getRideById_PassengerCannotViewOtherRide() {
        String token = "Bearer token2";
        AccountDTO otherPassenger = new AccountDTO();
        otherPassenger.setId(99L);
        otherPassenger.setRole("PASSENGER");

        when(rideService.getRideResponseById(100L)).thenReturn(rideResponse);
        when(accountClient.getCurrentAccount(token)).thenReturn(otherPassenger);

        assertThrows(UnauthorizedRideOperationException.class, () -> {
            rideController.getRideById(100L, token);
        });
    }

    @Test
    void getRideById_DriverCanViewAssignedRide() {
        String token = "Bearer token3";
        when(rideService.getRideResponseById(100L)).thenReturn(rideResponse);
        when(accountClient.getCurrentAccount(token)).thenReturn(driverAccount);

        ResponseEntity<RideResponse> response = rideController.getRideById(100L, token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(100L, response.getBody().getId());
    }

    @Test
    void getRideById_DriverCannotViewUnassignedRide() {
        String token = "Bearer token4";
        AccountDTO otherDriver = new AccountDTO();
        otherDriver.setId(88L);
        otherDriver.setRole("DRIVER");

        when(rideService.getRideResponseById(100L)).thenReturn(rideResponse);
        when(accountClient.getCurrentAccount(token)).thenReturn(otherDriver);

        assertThrows(UnauthorizedRideOperationException.class, () -> {
            rideController.getRideById(100L, token);
        });
    }

    @Test
    void getRidesByPassenger_PassengerCannotViewOtherPassengerRides() {
        String token = "Bearer token5";
        AccountDTO otherPassenger = new AccountDTO();
        otherPassenger.setId(99L);
        otherPassenger.setRole("PASSENGER");

        when(accountClient.getCurrentAccount(token)).thenReturn(otherPassenger);

        assertThrows(UnauthorizedRideOperationException.class, () -> {
            rideController.getRidesByPassenger(1L, token);
        });
    }
}
