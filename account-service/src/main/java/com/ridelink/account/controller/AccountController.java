package com.ridelink.account.controller;

import com.ridelink.account.dto.RegisterRequest;
import com.ridelink.account.entity.Account;
import com.ridelink.account.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.ridelink.account.dto.LoginRequest;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Account register(@Valid @RequestBody RegisterRequest request) {
        return accountService.register(request);
    }
   @PostMapping("/login")
public String login(@Valid @RequestBody LoginRequest request) {
    return accountService.login(request);
} 
}