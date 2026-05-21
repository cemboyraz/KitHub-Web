package com.kithub.dto;

import com.kithub.model.ReadingStatus;
import java.time.LocalDateTime;

public record UserBookResponse(
        Long id,
        String googleBooksId,
        String title,
        String author,
        String imageUrl,
        String summary,
        ReadingStatus status,
        LocalDateTime addedAt
) {}