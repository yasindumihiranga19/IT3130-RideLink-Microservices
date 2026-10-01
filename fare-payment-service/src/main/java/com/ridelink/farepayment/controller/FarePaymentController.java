package com.ridelink.farepayment.controller;

import com.ridelink.farepayment.dto.FareEstimateRequest;
import com.ridelink.farepayment.dto.FareCalculateRequest;
import com.ridelink.farepayment.dto.FareResponseDTO;
import com.ridelink.farepayment.entity.Payment;
import com.ridelink.farepayment.service.FarePaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;

import java.util.UUID;

@RestController
@RequestMapping("/api/fares")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class FarePaymentController {

    private final FarePaymentService farePaymentService;

    // Workflow 3: Fare estimation
    @Operation(summary = "Estimate fare", description = "Calculates an estimated fare for a ride based on pickup and destination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully estimated fare"),
            @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    @PreAuthorize("hasAnyRole('PASSENGER', 'DRIVER')")
    @PostMapping("/estimate")
    public ResponseEntity<FareResponseDTO> getEstimate(@Valid @RequestBody FareEstimateRequest request) {
        FareResponseDTO estimate = farePaymentService.estimateFare(request);
        return ResponseEntity.ok(estimate);
    }

    // Final Fare Calculation
    @Operation(summary = "Calculate final fare", description = "Calculates the final fare of a completed ride")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully calculated final fare"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "404", description = "Ride not found")
    })
    @PreAuthorize("hasRole('DRIVER')")
    @PostMapping("/calculate")
    public ResponseEntity<FareResponseDTO> calculateFinalFare(@Valid @RequestBody FareCalculateRequest request,
                                                              @RequestHeader(value = "Authorization", required = false) String token) {
        FareResponseDTO finalPayment = farePaymentService.calculateFinalFare(request, token);
        return ResponseEntity.ok(finalPayment);
    }

    // Workflow 6: Completion and payment
    @Operation(summary = "Process payment", description = "Processes payment for a ride")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment processed successfully"),
            @ApiResponse(responseCode = "400", description = "Payment processing failed")
    })
    @PreAuthorize("hasRole('PASSENGER')")
    @PostMapping("/pay")
    public ResponseEntity<Payment> processPayment(@RequestBody Payment paymentDetails) {
        Payment processedPayment = farePaymentService.processPayment(paymentDetails);
        
        if ("FAILED".equals(processedPayment.getStatus())) {
             return ResponseEntity.badRequest().body(processedPayment);
        }
        return ResponseEntity.ok(processedPayment);
    }

    // Workflow 6: retrieve a receipt/payment record
    @Operation(summary = "Get receipt", description = "Retrieves a payment receipt by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved receipt"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @PreAuthorize("hasAnyRole('PASSENGER', 'DRIVER')")
    @GetMapping("/receipt/{paymentId}")
    public ResponseEntity<Payment> getReceipt(@PathVariable UUID paymentId) {
        Payment receipt = farePaymentService.getReceipt(paymentId);
        return ResponseEntity.ok(receipt);
    }
}
