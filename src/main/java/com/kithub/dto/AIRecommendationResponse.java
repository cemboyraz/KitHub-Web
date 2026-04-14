package com.kithub.dto;

public record AIRecommendationResponse(
        Long recommendationId,
        BookResponse book,
        Double matchScore,
        String aiReasoning
) {}