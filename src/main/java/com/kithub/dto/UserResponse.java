package com.kithub.dto;

import com.kithub.model.Role;

public record UserResponse(
        Long id,
        String username,
        String email,
        Role role
) {}