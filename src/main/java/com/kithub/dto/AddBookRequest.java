package com.kithub.dto;

public record AddBookRequest(
        String googleBooksId,
        String title,
        String author,
        String summary,    // Veritabanına kaydetmek için şart
        String imageUrl,
        String category,
        String status
) {}