package com.ridelink.ridemanagement.client;

import com.ridelink.ridemanagement.dto.DriverDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class DriverClient {

    private final RestTemplate restTemplate;
    private final String driverServiceUrl;

    public DriverClient(RestTemplate restTemplate, @Value("${services.driver-service.url}") String driverServiceUrl) {
        this.restTemplate = restTemplate;
        this.driverServiceUrl = driverServiceUrl;
    }

    public List<DriverDTO> getAvailableDrivers(String token) {
        HttpHeaders headers = new HttpHeaders();
        if (token != null && token.startsWith("Bearer ")) {
            headers.set("Authorization", token);
        } else if (token != null) {
            headers.set("Authorization", "Bearer " + token);
        }
        
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        ResponseEntity<List<DriverDTO>> response = restTemplate.exchange(
                driverServiceUrl + "/api/drivers/available",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<DriverDTO>>() {}
        );
        
        return response.getBody();
    }
    
    public void updateAvailability(Long driverId, String status, String token) {
        HttpHeaders headers = new HttpHeaders();
        if (token != null && token.startsWith("Bearer ")) {
            headers.set("Authorization", token);
        } else if (token != null) {
            headers.set("Authorization", "Bearer " + token);
        }
        
        java.util.Map<String, String> body = java.util.Map.of("availabilityStatus", status);
        HttpEntity<java.util.Map<String, String>> entity = new HttpEntity<>(body, headers);
        
        try {
            restTemplate.exchange(
                    driverServiceUrl + "/api/drivers/" + driverId + "/availability",
                    HttpMethod.PATCH,
                    entity,
                    Void.class
            );
        } catch (Exception e) {
            System.err.println("Failed to update driver availability: " + e.getMessage());
        }
    }
    
}
