package com.kithub.dto;

public record OllamaResponse(
        String model,
        String created_at,
        String response,
        boolean done
) {}