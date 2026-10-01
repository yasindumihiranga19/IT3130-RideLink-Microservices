package com.ridelink.farepayment.client;

import com.ridelink.farepayment.dto.RideResponseDto;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@Component
public class RideServiceClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public RideResponseDto getRideDetails(Long rideId, String token) {
        String url = "http://localhost:8083/api/rides/" + rideId;
        HttpHeaders headers = new HttpHeaders();
        if (token != null && token.startsWith("Bearer ")) {
            headers.set("Authorization", token);
        } else if (token != null) {
            headers.set("Authorization", "Bearer " + token);
        }
        
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<RideResponseDto> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                RideResponseDto.class
        );
        return response.getBody();
    }
}
