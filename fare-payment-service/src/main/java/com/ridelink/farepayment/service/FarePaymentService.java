package com.ridelink.farepayment.service;

import com.ridelink.farepayment.dto.FareEstimateRequest;
import com.ridelink.farepayment.dto.FareEstimateResponse;
import com.ridelink.farepayment.entity.Payment;
import com.ridelink.farepayment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class FarePaymentService {

    private final PaymentRepository paymentRepository;
    
    // Assignment Requirement: clearly documented calculation rule (Updated for Rupees)
    private static final BigDecimal BASE_FARE = new BigDecimal("300.00");
    private static final BigDecimal PER_KM_RATE = new BigDecimal("100.00");
    private static final BigDecimal PER_MINUTE_WAIT_RATE = new BigDecimal("20.00");

    public FareEstimateResponse estimateFare(FareEstimateRequest request) {
        BigDecimal distance = BigDecimal.valueOf(request.getSimulatedDistanceInKm());
        BigDecimal totalFare = BASE_FARE.add(distance.multiply(PER_KM_RATE));
        
        return new FareEstimateResponse(
                totalFare.setScale(2, RoundingMode.HALF_UP),
                "LKR", // Changed to Rupees
                "Estimated fare based on " + request.getSimulatedDistanceInKm() + "km"
        );
    }

    // Final Fare Calculation using MOCK DATA (Before interacting with Ride Service)
    public Payment calculateFinalFare(UUID rideId, UUID passengerId) {
        // 1. MOCK DATA: Pretend we called the Ride Management Service and got these actual values
        double mockActualDistanceKm = 12.5; 
        double mockWaitTimeMinutes = 5.0;   
        
        // 2. Final Fare Calculation Rule
        BigDecimal distanceFare = BigDecimal.valueOf(mockActualDistanceKm).multiply(PER_KM_RATE);
        BigDecimal waitTimeFare = BigDecimal.valueOf(mockWaitTimeMinutes).multiply(PER_MINUTE_WAIT_RATE);
        
        BigDecimal finalAmount = BASE_FARE.add(distanceFare).add(waitTimeFare).setScale(2, RoundingMode.HALF_UP);
        
        // 3. Create the payment record (Status is PENDING until processPayment is called)
        Payment payment = new Payment();
        payment.setRideId(rideId);
        payment.setPassengerId(passengerId);
        payment.setAmount(finalAmount);
        payment.setCurrency("LKR"); // Set currency to Rupees
        payment.setStatus("PENDING");
        payment.setPaymentMethod("CREDIT_CARD"); // Default mock method
        
        return paymentRepository.save(payment);
    }

    public Payment processPayment(Payment payment) {
        // Assignment Requirement: Simulated payment recording and random negative scenarios
        
        // Simulate a 10% chance of payment failure for "Negative scenarios" demonstration
        boolean isSuccess = new Random().nextInt(100) > 10;
        
        if (isSuccess) {
            payment.setStatus("COMPLETED");
        } else {
            payment.setStatus("FAILED");
        }
        
        return paymentRepository.save(payment);
    }
    
    public Payment getReceipt(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }
}
