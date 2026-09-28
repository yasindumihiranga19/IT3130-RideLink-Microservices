package com.ridelink.farepayment.controller;

import com.ridelink.farepayment.dto.FareEstimateRequest;
import com.ridelink.farepayment.dto.FareEstimateResponse;
import com.ridelink.farepayment.entity.Payment;
import com.ridelink.farepayment.service.FarePaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fares")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class FarePaymentController {

    private final FarePaymentService farePaymentService;

    // Workflow 3: Fare estimation
    @PostMapping("/estimate")
    public ResponseEntity<FareEstimateResponse> getEstimate(@RequestBody FareEstimateRequest request) {
        FareEstimateResponse estimate = farePaymentService.estimateFare(request);
        return ResponseEntity.ok(estimate);
    }

    // Final Fare Calculation (Using Mock Data)
    @PostMapping("/calculate-final")
    public ResponseEntity<Payment> calculateFinalFare(@RequestParam UUID rideId, @RequestParam UUID passengerId) {
        Payment finalPayment = farePaymentService.calculateFinalFare(rideId, passengerId);
        return ResponseEntity.ok(finalPayment);
    }

    // Workflow 6: Completion and payment
    @PostMapping("/pay")
    public ResponseEntity<Payment> processPayment(@RequestBody Payment paymentDetails) {
        Payment processedPayment = farePaymentService.processPayment(paymentDetails);
        
        if ("FAILED".equals(processedPayment.getStatus())) {
             return ResponseEntity.badRequest().body(processedPayment);
        }
        return ResponseEntity.ok(processedPayment);
    }

    // Workflow 6: retrieve a receipt/payment record
    @GetMapping("/receipt/{paymentId}")
    public ResponseEntity<Payment> getReceipt(@PathVariable UUID paymentId) {
        Payment receipt = farePaymentService.getReceipt(paymentId);
        return ResponseEntity.ok(receipt);
    }
}
