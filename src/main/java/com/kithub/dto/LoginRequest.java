package com.kithub.dto;

public record LoginRequest(
        String email,
        String password
) {}