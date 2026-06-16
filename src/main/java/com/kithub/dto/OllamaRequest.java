package com.kithub.dto;

public record OllamaRequest(
        String model,
        String prompt,
        boolean stream
) {}