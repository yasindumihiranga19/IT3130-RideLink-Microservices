package com.ridelink.drivervehicle.security;

import com.ridelink.drivervehicle.model.Driver;
import com.ridelink.drivervehicle.repository.DriverRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(properties = "jwt.secret=test-jwt-signing-secret-that-is-long-enough")
@AutoConfigureMockMvc
class DriverSecurityTest {

    private static final String TEST_SECRET = "test-jwt-signing-secret-that-is-long-enough";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DriverRepository driverRepository;

    @Test
    void protectedEndpoint_rejectsMissingToken() throws Exception {
        mockMvc.perform(get("/api/drivers/available"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_rejectsInvalidToken() throws Exception {
        mockMvc.perform(get("/api/drivers/available")
                        .header("Authorization", "Bearer not-a-valid-token"))
                .andExpect(status().isUnauthorized());
    }

            @Test
            void openApiDocumentsDriverOperationsAndBearerSecurity() throws Exception {
            mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/drivers/{id}/profile'].put.summary")
                    .value("Update driver profile"))
                .andExpect(jsonPath("$.paths['/api/drivers/{id}/vehicle'].get.summary")
                    .value("Get a driver's vehicle"))
                .andExpect(jsonPath("$.paths['/api/drivers/{id}/profile'].put.responses.200.description")
                    .value("Driver profile updated"))
                .andExpect(jsonPath("$.paths['/api/drivers/{id}/profile'].put.responses.401.description")
                    .value("Missing or invalid bearer token"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme")
                    .value("bearer"))
                .andExpect(jsonPath("$.paths['/api/drivers/{id}/profile'].put.security[0].bearerAuth")
                    .exists());
            }

    @Test
    void adminCanListDrivers() throws Exception {
        when(driverRepository.findAll()).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/drivers")
                        .header("Authorization", bearerToken("admin@example.com", "ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void driverCannotListAllDrivers() throws Exception {
        mockMvc.perform(get("/api/drivers")
                        .header("Authorization", bearerToken("driver@example.com", "DRIVER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void driverCanUpdateOwnServiceArea() throws Exception {
        Driver driver = driver(1L, "driver@example.com");
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(patch("/api/drivers/1/service-area")
                        .header("Authorization", bearerToken("driver@example.com", "DRIVER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"serviceArea\":\"Kandy\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void driverCannotUpdateAnotherDriversServiceArea() throws Exception {
        when(driverRepository.findById(1L))
                .thenReturn(Optional.of(driver(1L, "someone-else@example.com")));

        mockMvc.perform(patch("/api/drivers/1/service-area")
                        .header("Authorization", bearerToken("driver@example.com", "DRIVER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"serviceArea\":\"Kandy\"}"))
                .andExpect(status().isForbidden());
    }

            @Test
            void driverCanUpdateOwnProfile() throws Exception {
            Driver driver = driver(1L, "driver@example.com");
            when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
            when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

            mockMvc.perform(put("/api/drivers/1/profile")
                    .header("Authorization", bearerToken("driver@example.com", "DRIVER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"fullName\":\"Updated Driver\",\"phoneNumber\":\"0711234567\",\"licenseNumber\":\"B7654321\",\"serviceArea\":\"Kandy\"}"))
                .andExpect(status().isOk());
            }

            @Test
            void driverCannotUpdateAnotherDriversProfile() throws Exception {
            when(driverRepository.findById(1L))
                .thenReturn(Optional.of(driver(1L, "someone-else@example.com")));

            mockMvc.perform(put("/api/drivers/1/profile")
                    .header("Authorization", bearerToken("driver@example.com", "DRIVER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"fullName\":\"Updated Driver\",\"phoneNumber\":\"0711234567\",\"licenseNumber\":\"B7654321\"}"))
                .andExpect(status().isForbidden());
            }

            @Test
            void profileUpdate_rejectsBlankRequiredFields() throws Exception {
            Driver driver = driver(1L, "driver@example.com");
            when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));

            mockMvc.perform(put("/api/drivers/1/profile")
                    .header("Authorization", bearerToken("driver@example.com", "DRIVER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"fullName\":\" \",\"phoneNumber\":\"0711234567\",\"licenseNumber\":\"B7654321\"}"))
                .andExpect(status().isBadRequest());
            }

    private Driver driver(Long id, String email) {
        Driver driver = new Driver();
        driver.setId(id);
        driver.setEmail(email);
        driver.setFullName("Test Driver");
        driver.setPhoneNumber("0771234567");
        driver.setLicenseNumber("B1234567");
        return driver;
    }

    private String bearerToken(String email, String role) {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        return "Bearer " + Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();
    }
}