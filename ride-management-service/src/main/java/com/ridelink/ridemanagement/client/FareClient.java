package com.ridelink.ridemanagement.client;

import com.ridelink.ridemanagement.dto.FareResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class FareClient {

    private final RestTemplate restTemplate;
    private final String fareServiceUrl;

    public FareClient(RestTemplate restTemplate, @Value("${services.fare-service.url}") String fareServiceUrl) {
        this.restTemplate = restTemplate;
        this.fareServiceUrl = fareServiceUrl;
    }

    public FareResponseDTO calculateFare(Long rideId, String pickupLocation, String destinationLocation, String token) {
        HttpHeaders headers = new HttpHeaders();
        if (token != null && token.startsWith("Bearer ")) {
            headers.set("Authorization", token);
        } else if (token != null) {
            headers.set("Authorization", "Bearer " + token);
        }
        
        Map<String, Object> body = Map.of(
            "rideId", rideId,
            "pickupLocation", pickupLocation,
            "destinationLocation", destinationLocation
        );
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        
        ResponseEntity<FareResponseDTO> response = restTemplate.exchange(
                fareServiceUrl + "/api/fares/calculate",
                HttpMethod.POST,
                entity,
                FareResponseDTO.class
        );
        return response.getBody();
    }

    public FareResponseDTO estimateFare(String pickupLocation, String destinationLocation, String token) {
        HttpHeaders headers = new HttpHeaders();
        if (token != null && token.startsWith("Bearer ")) {
            headers.set("Authorization", token);
        } else if (token != null) {
            headers.set("Authorization", "Bearer " + token);
        }
        
        Map<String, Object> body = Map.of(
            "pickupLocation", pickupLocation,
            "destinationLocation", destinationLocation
        );
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        
        ResponseEntity<FareResponseDTO> response = restTemplate.exchange(
                fareServiceUrl + "/api/fares/estimate",
                HttpMethod.POST,
                entity,
                FareResponseDTO.class
        );
        return response.getBody();
    }
}
