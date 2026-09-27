package com.ridelink.account.service;

import com.ridelink.account.dto.RegisterRequest;
import com.ridelink.account.entity.Account;
import com.ridelink.account.repository.AccountRepository;
import org.springframework.stereotype.Service;

import com.ridelink.account.enums.AccountStatus;
import com.ridelink.account.enums.Role;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }
public Account register(RegisterRequest request) {

    if (accountRepository.existsByEmail(request.getEmail())) {
        throw new RuntimeException("Email already registered");
    }

    Account account = new Account();
    account.setName(request.getName());
    account.setEmail(request.getEmail());
    account.setPassword(request.getPassword());
    account.setRole(Role.PASSENGER);
    account.setStatus(AccountStatus.ACTIVE);

    return accountRepository.save(account);
  }    
}