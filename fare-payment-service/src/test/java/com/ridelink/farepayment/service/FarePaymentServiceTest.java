package com.ridelink.farepayment.service;

import com.ridelink.farepayment.dto.FareEstimateRequest;
import com.ridelink.farepayment.dto.FareCalculateRequest;
import com.ridelink.farepayment.dto.FareResponseDTO;
import com.ridelink.farepayment.dto.RideResponseDto;
import com.ridelink.farepayment.entity.Payment;
import com.ridelink.farepayment.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import com.ridelink.farepayment.client.RideServiceClient;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
public class FarePaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RideServiceClient rideServiceClient;

    @InjectMocks
    private FarePaymentService farePaymentService;

    @Test
    public void testEstimateFare() {
        // Arrange
        FareEstimateRequest request = new FareEstimateRequest();
        request.setSimulatedDistanceInKm(10.0);
        // Base Fare (300.00) + (10.0 * 100.00) = 1300.00

        // Act
        FareResponseDTO response = farePaymentService.estimateFare(request);

        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal("1300.00"), response.getEstimatedFare());
    }

    @Test
    public void testCalculateFinalFare() {
        // Arrange
        Long rideId = 1L;
        Long passengerId = 2L;
        
        FareCalculateRequest request = new FareCalculateRequest();
        request.setRideId(rideId);
        request.setPickupLocation("A");
        request.setDestinationLocation("B");
        
        RideResponseDto mockRide = new RideResponseDto();
        mockRide.setPassengerId(passengerId);
        mockRide.setPickupLocation("A");
        mockRide.setDestinationLocation("B");
        
        when(rideServiceClient.getRideDetails(eq(rideId), any())).thenReturn(mockRide);
        
        // Mocking the save behavior to just return what was passed in
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        FareResponseDTO finalPayment = farePaymentService.calculateFinalFare(request, "mock_token");

        // Assert
        assertNotNull(finalPayment);
        // Since both strings have length 1, distance = 0 * 1.5 + 2.0 = 2.0 km
        // Logic: Base (300.00) + (2.0km * 100.00 = 200.00) + (5mins * 20.00 = 100.00) = 600.00
        assertEquals(new BigDecimal("600.00"), finalPayment.getFinalFare());
    }
}
