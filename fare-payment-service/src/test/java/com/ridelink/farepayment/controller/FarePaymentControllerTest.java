package com.ridelink.farepayment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.farepayment.dto.FareCalculateRequest;
import com.ridelink.farepayment.dto.FareEstimateRequest;
import com.ridelink.farepayment.dto.FareResponseDTO;

import com.ridelink.farepayment.service.FarePaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "jwt.secret=test-jwt-signing-secret-that-is-long-enough")
@AutoConfigureMockMvc(addFilters = false)
public class FarePaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private FarePaymentService farePaymentService;

    @Test
    @WithMockUser(roles = "PASSENGER")
    public void testGetEstimate_Success() throws Exception {
        FareEstimateRequest request = new FareEstimateRequest();
        request.setPickupLocation("A");
        request.setDestinationLocation("B");
        request.setSimulatedDistanceInKm(5.0);

        FareResponseDTO responseDTO = FareResponseDTO.builder().estimatedFare(new BigDecimal("800.00")).build();
        when(farePaymentService.estimateFare(any(FareEstimateRequest.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/fares/estimate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estimatedFare").value("800.0"));
    }

    @Test
    @WithMockUser(roles = "PASSENGER")
    public void testGetEstimate_ValidationFailure() throws Exception {
        // Missing pickupLocation and destinationLocation
        FareEstimateRequest request = new FareEstimateRequest();
        request.setSimulatedDistanceInKm(-5.0); // Invalid negative distance

        mockMvc.perform(post("/api/fares/estimate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.pickupLocation").exists())
                .andExpect(jsonPath("$.destinationLocation").exists())
                .andExpect(jsonPath("$.simulatedDistanceInKm").exists());
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    public void testCalculateFinalFare_Success() throws Exception {
        FareCalculateRequest request = new FareCalculateRequest();
        request.setRideId(1L);

        FareResponseDTO responseDTO = FareResponseDTO.builder().finalFare(new BigDecimal("1000.00")).build();
        when(farePaymentService.calculateFinalFare(any(), any())).thenReturn(responseDTO);

        mockMvc.perform(post("/api/fares/calculate")
                        .with(csrf())
                        .header("Authorization", "Bearer mocktoken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.finalFare").value("1000.0"));
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    public void testCalculateFinalFare_ValidationFailure() throws Exception {
        FareCalculateRequest request = new FareCalculateRequest();
        // Missing rideId

        mockMvc.perform(post("/api/fares/calculate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.rideId").exists());
    }
}
