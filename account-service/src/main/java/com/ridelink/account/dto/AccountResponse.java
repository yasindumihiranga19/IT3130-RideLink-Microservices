package com.ridelink.account.dto;

import com.ridelink.account.entity.Account;
import com.ridelink.account.enums.AccountStatus;
import com.ridelink.account.enums.Role;

public class AccountResponse {

    private Long id;
    private String name;
    private String email;
    private Role role;
    private AccountStatus status;

    public AccountResponse(Long id, String name, String email,
                           Role role, AccountStatus status) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.status = status;
    }

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getName(),
                account.getEmail(),
                account.getRole(),
                account.getStatus()
        );
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public AccountStatus getStatus() {
        return status;
    }
}