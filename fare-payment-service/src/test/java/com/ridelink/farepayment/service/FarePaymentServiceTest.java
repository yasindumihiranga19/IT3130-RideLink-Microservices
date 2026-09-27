package com.ridelink.farepayment.service;

import com.ridelink.farepayment.dto.FareEstimateRequest;
import com.ridelink.farepayment.dto.FareEstimateResponse;
import com.ridelink.farepayment.entity.Payment;
import com.ridelink.farepayment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FarePaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private FarePaymentService farePaymentService;

    @Test
    public void testEstimateFare() {
        // Arrange
        FareEstimateRequest request = new FareEstimateRequest();
        request.setSimulatedDistanceInKm(10.0);
        // Base Fare (300.00) + (10.0 * 100.00) = 1300.00

        // Act
        FareEstimateResponse response = farePaymentService.estimateFare(request);

        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal("1300.00"), response.getEstimatedAmount());
        assertEquals("LKR", response.getCurrency());
    }

    @Test
    public void testCalculateFinalFare() {
        // Arrange
        UUID rideId = UUID.randomUUID();
        UUID passengerId = UUID.randomUUID();
        
        // Mocking the save behavior to just return what was passed in
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Payment finalPayment = farePaymentService.calculateFinalFare(rideId, passengerId);

        // Assert
        // Logic: Base (300.00) + (12.5km * 100.00 = 1250.00) + (5mins * 20.00 = 100.00) = 1650.00
        assertNotNull(finalPayment);
        assertEquals(new BigDecimal("1650.00"), finalPayment.getAmount());
        assertEquals("PENDING", finalPayment.getStatus());
        assertEquals(rideId, finalPayment.getRideId());
    }
}
