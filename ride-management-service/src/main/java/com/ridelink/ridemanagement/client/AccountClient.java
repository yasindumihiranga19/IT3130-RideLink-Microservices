package com.ridelink.ridemanagement.client;

import com.ridelink.ridemanagement.dto.AccountDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AccountClient {

    private final RestTemplate restTemplate;
    private final String accountServiceUrl;

    public AccountClient(RestTemplate restTemplate, @Value("${services.account-service.url}") String accountServiceUrl) {
        this.restTemplate = restTemplate;
        this.accountServiceUrl = accountServiceUrl;
    }

    public AccountDTO getCurrentAccount(String token) {
        HttpHeaders headers = new HttpHeaders();
        if (token != null && token.startsWith("Bearer ")) {
            headers.set("Authorization", token);
        } else if (token != null) {
            headers.set("Authorization", "Bearer " + token);
        }
        
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        ResponseEntity<AccountDTO> response = restTemplate.exchange(
                accountServiceUrl + "/api/accounts/profile",
                HttpMethod.GET,
                entity,
                AccountDTO.class
        );
        
        return response.getBody();
    }
}
