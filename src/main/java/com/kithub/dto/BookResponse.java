package com.kithub.dto;



public record BookResponse(
        Long id,
        String googleBooksId,
        String title,
        String author,
        String imageUrl,
        String category,
        Float averageRating
) {}