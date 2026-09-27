package com.ridelink.farepayment.client;

import com.ridelink.farepayment.dto.RideDetailsDto;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.UUID;

// This client will call Member 3's Ride Management Service using Spring's built-in RestTemplate
@Component
public class RideServiceClient {

    private final RestTemplate restTemplate = new RestTemplate();

    // The API contract we agreed upon with Member 3
    public RideDetailsDto getRideDetails(UUID rideId) {
        String url = "http://localhost:8083/api/v1/rides/" + rideId + "/details";
        return restTemplate.getForObject(url, RideDetailsDto.class);
    }
}
