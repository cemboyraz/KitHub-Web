package com.kithub.dto;

public record BookResponse(
        String id,
        String title,
        String author,
        String imageUrl,
        String category,
        Float averageRating
) {}