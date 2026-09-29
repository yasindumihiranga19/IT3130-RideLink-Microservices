package com.ridelink.drivervehicle.controller;

import com.ridelink.drivervehicle.exception.ConflictException;
import com.ridelink.drivervehicle.dto.DriverProfileUpdateRequest;
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
        driver.setServiceArea("  Colombo  ");
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);

        ResponseEntity<Driver> response = driverController.registerDriver(driver);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Kasun Perera", response.getBody().getFullName());
        assertEquals("Colombo", response.getBody().getServiceArea());
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
    void updateProfile_updatesEditableFieldsAndPreservesAccountAndVehicleData() {
        Driver driver = sampleDriver();
        driver.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        driver.setCurrentLatitude(6.9271);
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleNumber("ABC-123");
        driver.setVehicle(vehicle);
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(driverRepository.save(driver)).thenReturn(driver);

        DriverProfileUpdateRequest request = new DriverProfileUpdateRequest(
                "  Nimal Perera  ", "  0711234567  ", "  B7654321  ", "  Kandy  ");

        ResponseEntity<Driver> response = driverController.updateProfile(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Nimal Perera", response.getBody().getFullName());
        assertEquals("0711234567", response.getBody().getPhoneNumber());
        assertEquals("B7654321", response.getBody().getLicenseNumber());
        assertEquals("Kandy", response.getBody().getServiceArea());
        assertEquals("kasun@example.com", response.getBody().getEmail());
        assertEquals(AvailabilityStatus.AVAILABLE, response.getBody().getAvailabilityStatus());
        assertEquals(6.9271, response.getBody().getCurrentLatitude());
        assertSame(vehicle, response.getBody().getVehicle());
        verify(driverRepository).save(driver);
    }

    @Test
    void updateProfile_returnsNotFound_whenDriverMissing() {
        when(driverRepository.findById(999L)).thenReturn(Optional.empty());
        DriverProfileUpdateRequest request = new DriverProfileUpdateRequest(
                "Nimal Perera", "0711234567", "B7654321", "Kandy");

        ResponseEntity<Driver> response = driverController.updateProfile(999L, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(driverRepository, never()).save(any(Driver.class));
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
    void updateLocation_changesCoordinates() {
        Driver driver = sampleDriver();
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(driverRepository.save(driver)).thenReturn(driver);

        ResponseEntity<Driver> response = driverController.updateLocation(
                1L, Map.of("latitude", 6.9271, "longitude", 79.8612));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(6.9271, response.getBody().getCurrentLatitude());
        assertEquals(79.8612, response.getBody().getCurrentLongitude());
        verify(driverRepository).save(driver);
    }

    @Test
    void updateServiceArea_changesArea() {
        Driver driver = sampleDriver();
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(driverRepository.save(driver)).thenReturn(driver);

        ResponseEntity<Driver> response = driverController.updateServiceArea(
            1L, Map.of("serviceArea", "  Kandy  "));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Kandy", response.getBody().getServiceArea());
        verify(driverRepository).save(driver);
    }

    @Test
    void updateServiceArea_returnsNotFound_whenDriverMissing() {
        when(driverRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseEntity<Driver> response = driverController.updateServiceArea(
                999L, Map.of("serviceArea", "Kandy"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(driverRepository, never()).save(any(Driver.class));
    }

    @Test
    void updateServiceArea_returnsBadRequest_whenAreaBlank() {
        when(driverRepository.findById(1L)).thenReturn(Optional.of(sampleDriver()));

        ResponseEntity<Driver> response = driverController.updateServiceArea(
                1L, Map.of("serviceArea", " "));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(driverRepository, never()).save(any(Driver.class));
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
        when(driverRepository.findByServiceAreaIgnoreCaseAndAvailabilityStatus("colombo", AvailabilityStatus.AVAILABLE))
                .thenReturn(List.of(sampleDriver()));

        ResponseEntity<List<Driver>> response = driverController.getAvailableDrivers(" colombo ");

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

    @Test
    void getVehicle_returnsVehicle_whenAssigned() {
        Driver driver = sampleDriver();
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleNumber("ABC-123");
        driver.setVehicle(vehicle);
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));

        ResponseEntity<Vehicle> response = driverController.getVehicle(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ABC-123", response.getBody().getVehicleNumber());
    }

    @Test
    void getVehicle_returnsNotFound_whenDriverOrVehicleMissing() {
        Driver driver = sampleDriver();
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(driverRepository.findById(999L)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND, driverController.getVehicle(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, driverController.getVehicle(999L).getStatusCode());
    }

    @Test
    void updateVehicle_updatesAssignedVehicle() {
        Driver driver = sampleDriver();
        Vehicle existingVehicle = new Vehicle();
        existingVehicle.setVehicleNumber("OLD-123");
        driver.setVehicle(existingVehicle);
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(driverRepository.save(driver)).thenReturn(driver);

        Vehicle update = new Vehicle();
        update.setVehicleNumber("NEW-456");
        update.setType("CAR");
        update.setModel("Sedan");
        update.setColor("Blue");
        update.setCapacity(4);

        ResponseEntity<Vehicle> response = driverController.updateVehicle(1L, update);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("NEW-456", response.getBody().getVehicleNumber());
        assertEquals("CAR", response.getBody().getType());
        assertSame(existingVehicle, response.getBody());
        verify(driverRepository).save(driver);
    }

    @Test
    void updateVehicle_returnsNotFound_whenNoAssignedVehicle() {
        Driver driver = sampleDriver();
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));

        ResponseEntity<Vehicle> response = driverController.updateVehicle(1L, new Vehicle());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(driverRepository, never()).save(any(Driver.class));
    }
}