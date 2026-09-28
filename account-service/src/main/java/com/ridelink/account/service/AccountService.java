package com.ridelink.account.service;

import com.ridelink.account.dto.AccountResponse;
import com.ridelink.account.dto.LoginRequest;
import com.ridelink.account.dto.ProfileUpdateRequest;
import com.ridelink.account.dto.RegisterRequest;
import com.ridelink.account.dto.RoleUpdateRequest;
import com.ridelink.account.dto.StatusUpdateRequest;
import com.ridelink.account.entity.Account;
import com.ridelink.account.enums.AccountStatus;
import com.ridelink.account.enums.Role;
import com.ridelink.account.exception.DuplicateResourceException;
import com.ridelink.account.exception.InvalidCredentialsException;
import com.ridelink.account.exception.ResourceNotFoundException;
import com.ridelink.account.repository.AccountRepository;
import com.ridelink.account.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AccountService(
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    
    // REGISTER
    

    public AccountResponse register(RegisterRequest request) {

        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Email already registered"
            );
        }

        Account account = new Account();

        account.setName(request.getName());
        account.setEmail(request.getEmail());

        // Store encrypted password
        account.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        // New users are passengers by default
        account.setRole(Role.PASSENGER);

        // New accounts are active by default
        account.setStatus(AccountStatus.ACTIVE);

        Account savedAccount = accountRepository.save(account);

        // Never return the Account entity because it contains password
        return AccountResponse.from(savedAccount);
    }

  
    // LOGIN
    

    public String login(LoginRequest request) {

        Account account = accountRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "Invalid email or password"
                        )
                );

        if (!passwordEncoder.matches(
                request.getPassword(),
                account.getPassword())) {

            throw new InvalidCredentialsException(
                    "Invalid email or password"
            );
        }

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidCredentialsException(
                    "Account is not active"
            );
        }

        return jwtService.generateToken(account.getEmail());
    }

    
    // GET PROFILE
    

    public AccountResponse getProfile(String email) {

        Account account = accountRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account not found"
                        )
                );

        return AccountResponse.from(account);
    }

   
    // UPDATE PROFILE
    

    public AccountResponse updateProfile(
            String currentEmail,
            ProfileUpdateRequest request) {

        Account account = accountRepository
                .findByEmail(currentEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account not found"
                        )
                );

        // Update name
        if (request.getName() != null &&
                !request.getName().isBlank()) {

            account.setName(request.getName());
        }

        // Update email
        if (request.getEmail() != null &&
                !request.getEmail().isBlank() &&
                !request.getEmail().equals(currentEmail)) {

            if (accountRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException(
                        "Email already registered"
                );
            }

            account.setEmail(request.getEmail());
        }

        // Update password
        if (request.getPassword() != null &&
                !request.getPassword().isBlank()) {

            account.setPassword(
                    passwordEncoder.encode(request.getPassword())
            );
        }

        Account updatedAccount = accountRepository.save(account);

        return AccountResponse.from(updatedAccount);
    }

    
    // UPDATE STATUS
    

    public AccountResponse updateStatus(
            Long accountId,
            StatusUpdateRequest request) {

        Account account = accountRepository
                .findById(accountId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account not found"
                        )
                );

        account.setStatus(request.getStatus());

        Account updatedAccount =
                accountRepository.save(account);

        return AccountResponse.from(updatedAccount);
    }

    // UPDATE ROLE


    public AccountResponse updateRole(
            Long accountId,
            RoleUpdateRequest request) {

        Account account = accountRepository
                .findById(accountId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account not found"
                        )
                );

        account.setRole(request.getRole());

        Account updatedAccount =
                accountRepository.save(account);

        return AccountResponse.from(updatedAccount);
    }
}