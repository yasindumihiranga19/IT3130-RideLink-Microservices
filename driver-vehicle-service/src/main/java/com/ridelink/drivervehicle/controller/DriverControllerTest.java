package com.ridelink.drivervehicle.controller;

import com.ridelink.drivervehicle.exception.ConflictException;
import com.ridelink.drivervehicle.model.AvailabilityStatus;
import com.ridelink.drivervehicle.model.Driver;
import com.ridelink.drivervehicle.model.Vehicle;
import com.ridelink.drivervehicle.repository.DriverRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverControllerTest {

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private DriverController driverController;

    private Driver sampleDriver() {
        Driver driver = new Driver();
        driver.setId(1L);
        driver.setFullName("Kasun Perera");
        driver.setEmail("kasun@example.com");
        driver.setPhoneNumber("0771234567");
        driver.setLicenseNumber("B1234567");
        driver.setServiceArea("Colombo");
        return driver;
    }

    @Test
    void registerDriver_returnsCreated() {
        Driver driver = sampleDriver();
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);

        ResponseEntity<Driver> response = driverController.registerDriver(driver);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Kasun Perera", response.getBody().getFullName());
    }

    @Test
    void getDriverById_returnsDriver_whenExists() {
        when(driverRepository.findById(1L)).thenReturn(Optional.of(sampleDriver()));

        ResponseEntity<Driver> response = driverController.getDriverById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void getDriverById_returnsNotFound_whenMissing() {
        when(driverRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseEntity<Driver> response = driverController.getDriverById(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void updateAvailability_changesStatus() {
        Driver driver = sampleDriver();
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);

        ResponseEntity<Driver> response = driverController.updateAvailability(
                1L, Map.of("availabilityStatus", "AVAILABLE"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(AvailabilityStatus.AVAILABLE, response.getBody().getAvailabilityStatus());
    }

    @Test
    void updateAvailability_invalidStatus_returnsBadRequest() {
        when(driverRepository.findById(1L)).thenReturn(Optional.of(sampleDriver()));

        ResponseEntity<Driver> response = driverController.updateAvailability(
                1L, Map.of("availabilityStatus", "BUSY"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(driverRepository, never()).save(any(Driver.class));
    }

    @Test
    void updateAvailability_returnsNotFound_whenDriverMissing() {
        when(driverRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseEntity<Driver> response = driverController.updateAvailability(
                999L, Map.of("availabilityStatus", "AVAILABLE"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getAvailableDrivers_withoutServiceArea_returnsAllAvailable() {
        when(driverRepository.findByAvailabilityStatus(AvailabilityStatus.AVAILABLE))
                .thenReturn(List.of(sampleDriver()));

        ResponseEntity<List<Driver>> response = driverController.getAvailableDrivers(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getAvailableDrivers_withServiceArea_filtersByArea() {
        when(driverRepository.findByServiceAreaAndAvailabilityStatus("Colombo", AvailabilityStatus.AVAILABLE))
                .thenReturn(List.of(sampleDriver()));

        ResponseEntity<List<Driver>> response = driverController.getAvailableDrivers("Colombo");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Colombo", response.getBody().get(0).getServiceArea());
    }

    @Test
    void addVehicle_returnsCreated_whenDriverHasNoVehicle() {
        Driver driver = sampleDriver();
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);

        ResponseEntity<Driver> response = driverController.addVehicle(1L, new Vehicle());

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(driverRepository).save(driver);
    }

    @Test
    void addVehicle_throwsConflict_whenDriverAlreadyHasVehicle() {
        Driver driver = sampleDriver();
        driver.setVehicle(new Vehicle());
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));

        assertThrows(ConflictException.class,
                () -> driverController.addVehicle(1L, new Vehicle()));
        verify(driverRepository, never()).save(any(Driver.class));
    }
}