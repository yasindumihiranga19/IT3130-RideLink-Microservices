package com.ridelink.farepayment.service;

import com.ridelink.farepayment.client.RideServiceClient;
import com.ridelink.farepayment.dto.FareEstimateRequest;
import com.ridelink.farepayment.dto.FareResponseDTO;
import com.ridelink.farepayment.dto.FareCalculateRequest;
import com.ridelink.farepayment.dto.RideResponseDto;
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
    private final RideServiceClient rideServiceClient;
    
    // Assignment Requirement: clearly documented calculation rule (Updated for Rupees)
    private static final BigDecimal BASE_FARE = new BigDecimal("300.00");
    private static final BigDecimal PER_KM_RATE = new BigDecimal("100.00");
    private static final BigDecimal PER_MINUTE_WAIT_RATE = new BigDecimal("20.00");

    public FareResponseDTO estimateFare(FareEstimateRequest request) {
        double dist = request.getSimulatedDistanceInKm();
        if (dist <= 0 && request.getPickupLocation() != null && request.getDestinationLocation() != null) {
            dist = Math.abs(request.getPickupLocation().length() - request.getDestinationLocation().length()) * 1.5 + 2.0;
        }
        BigDecimal distance = BigDecimal.valueOf(dist);
        BigDecimal totalFare = BASE_FARE.add(distance.multiply(PER_KM_RATE));
        
        return FareResponseDTO.builder()
                .estimatedFare(totalFare.setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    public FareResponseDTO calculateFinalFare(FareCalculateRequest request, String token) {
        Long rideId = request.getRideId();
        double actualDistanceKm = 12.5; 
        double waitTimeMinutes = 5.0;   
        Long passengerId = null;
        
        try {
            System.out.println("Calling Ride Management Service for ride: " + rideId);
            RideResponseDto rideDetails = rideServiceClient.getRideDetails(rideId, token);
            passengerId = rideDetails.getPassengerId();
            
            // Generate simulated distance from location strings
            if (rideDetails.getPickupLocation() != null && rideDetails.getDestinationLocation() != null) {
                 actualDistanceKm = Math.abs(rideDetails.getPickupLocation().length() - rideDetails.getDestinationLocation().length()) * 1.5 + 2.0;
            } else if (request.getPickupLocation() != null && request.getDestinationLocation() != null) {
                 actualDistanceKm = Math.abs(request.getPickupLocation().length() - request.getDestinationLocation().length()) * 1.5 + 2.0;
            }
            System.out.println("Success! Got real data from Member 3.");
        } catch (Exception e) {
            System.out.println("Member 3's service failed! " + e.getMessage());
            throw new RuntimeException("Could not fetch ride details from Ride Management Service");
        }
        
        BigDecimal distanceFare = BigDecimal.valueOf(actualDistanceKm).multiply(PER_KM_RATE);
        BigDecimal waitTimeFare = BigDecimal.valueOf(waitTimeMinutes).multiply(PER_MINUTE_WAIT_RATE);
        
        BigDecimal finalAmount = BASE_FARE.add(distanceFare).add(waitTimeFare).setScale(2, RoundingMode.HALF_UP);
        
        Payment payment = new Payment();
        payment.setRideId(rideId);
        payment.setPassengerId(passengerId);
        payment.setAmount(finalAmount);
        payment.setCurrency("LKR");
        payment.setStatus("PENDING");
        payment.setPaymentMethod("CREDIT_CARD");
        
        paymentRepository.save(payment);
        
        return FareResponseDTO.builder()
                .finalFare(finalAmount)
                .build();
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
