package com.ridelink.ridemanagement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class FareServiceClient {

    private final RestTemplate restTemplate;

    @Value("${fare-payment-service.url:http://localhost:8084}")
    private String farePaymentServiceUrl;

    public Double getEstimatedFare(String pickupLocation, String destination) {
        log.info("Requesting fare estimate from Fare Payment Service for pickup: {} to destination: {}", pickupLocation, destination);
        try {
            // Assume the fare service has an endpoint like /api/v1/fares/estimate?pickup={pickup}&destination={destination}
            String url = farePaymentServiceUrl + "/api/v1/fares/estimate?pickup=" + pickupLocation + "&destination=" + destination;
            ResponseEntity<Double> response = restTemplate.getForEntity(url, Double.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                log.warn("Fare service returned non-200 status or null body. Defaulting to 15.50");
                return 15.50;
            }
        } catch (Exception e) {
            log.error("Failed to communicate with Fare Payment Service. Using fallback fare.", e);
            return 15.50; // Fallback fare in case of service failure
        }
    }
}
