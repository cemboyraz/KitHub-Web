package com.kithub.dto;

import com.kithub.model.Category;

public record BookResponse(
        Long id,
        String title,
        String author,
        Category category,
        String imageUrl,
        Float averageRating
) {}