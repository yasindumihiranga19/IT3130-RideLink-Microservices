package com.ridelink.account.controller;

import com.ridelink.account.dto.AccountResponse;
import com.ridelink.account.dto.LoginRequest;
import com.ridelink.account.dto.RegisterRequest;
import com.ridelink.account.dto.RoleUpdateRequest;
import com.ridelink.account.service.AccountService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.ridelink.account.dto.ProfileUpdateRequest;
import com.ridelink.account.dto.StatusUpdateRequest;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse register(
        @Valid @RequestBody RegisterRequest request) {

          return accountService.register(request);
    }

    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginRequest request) {
        return accountService.login(request);
    }

    @GetMapping("/profile")
    @SecurityRequirement(name = "bearerAuth")
    public AccountResponse getProfile(Authentication authentication) {

        String email = authentication.getName();

        return accountService.getProfile(email);
    }

    @PutMapping("/profile")
    @SecurityRequirement(name = "bearerAuth")
    public AccountResponse updateProfile(
        Authentication authentication,
        @Valid @RequestBody ProfileUpdateRequest request) {

       String currentEmail = authentication.getName();

       return accountService.updateProfile(currentEmail, request);
    }
    @PatchMapping("/{accountId}/status")
    @SecurityRequirement(name = "bearerAuth")
    public AccountResponse updateStatus(
        @PathVariable Long accountId,
        @Valid @RequestBody StatusUpdateRequest request) {

       return accountService.updateStatus(accountId, request);
    }
    @PatchMapping("/{accountId}/role")
    @SecurityRequirement(name = "bearerAuth")
    public AccountResponse updateRole(
        @PathVariable Long accountId,
        @Valid @RequestBody RoleUpdateRequest request) {

       return accountService.updateRole(accountId, request);
    }
}