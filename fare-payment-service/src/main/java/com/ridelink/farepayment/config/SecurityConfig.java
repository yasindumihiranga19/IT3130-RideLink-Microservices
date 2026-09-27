package com.ridelink.farepayment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import com.ridelink.farepayment.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Disable CSRF for REST APIs
            .csrf(AbstractHttpConfigurer::disable)
            // 2. Configure endpoint authorization
            .authorizeHttpRequests(auth -> auth
                // Allow Swagger UI access without tokens
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // Require a valid JWT token for all our actual API endpoints
                .requestMatchers("/api/v1/fares/**").authenticated()
                .anyRequest().authenticated()
            )
            // 3. Make session STATELESS (we use JWT instead of cookies)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 4. Add our custom JWT filter BEFORE the default Spring Security filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
