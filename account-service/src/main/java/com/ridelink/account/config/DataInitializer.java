package com.ridelink.account.config;

import com.ridelink.account.entity.Account;
import com.ridelink.account.enums.AccountStatus;
import com.ridelink.account.enums.Role;
import com.ridelink.account.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Bean
    CommandLineRunner createDefaultAdmin(
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            if (!accountRepository.existsByEmail(adminEmail)) {

                Account admin = new Account();

                admin.setName("RideLink Admin");
                admin.setEmail(adminEmail);
                admin.setPassword(
                        passwordEncoder.encode(adminPassword)
                );
                admin.setRole(Role.ADMIN);
                admin.setStatus(AccountStatus.ACTIVE);

                accountRepository.save(admin);

                System.out.println(
                        "Default ADMIN account created: "
                                + adminEmail
                );
            }
        };
    }
}