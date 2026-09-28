package com.ridelink.account.dto;

import com.ridelink.account.enums.Role;
import jakarta.validation.constraints.NotNull;

public class RoleUpdateRequest {

    @NotNull
    private Role role;

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}