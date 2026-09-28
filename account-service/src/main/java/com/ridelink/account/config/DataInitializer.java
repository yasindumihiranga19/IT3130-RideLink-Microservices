package com.ridelink.account.config;

import com.ridelink.account.entity.Account;
import com.ridelink.account.enums.AccountStatus;
import com.ridelink.account.enums.Role;
import com.ridelink.account.repository.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner createDefaultAdmin(
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            String adminEmail = "admin@ridelink.com";

            if (!accountRepository.existsByEmail(adminEmail)) {

                Account admin = new Account();

                admin.setName("RideLink Admin");
                admin.setEmail(adminEmail);
                admin.setPassword(
                        passwordEncoder.encode("Admin@12345")
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