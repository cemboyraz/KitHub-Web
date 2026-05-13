package com.kithub.dto;

import com.kithub.model.ReadingStatus;
import java.time.LocalDateTime;

public record UserBookResponse(
        Long id,
        String googleBooksId,    // kitabın google id'si detaya gitmek için lazım olabilir bence olamz da neyse
        String title,
        String author,
        String imageUrl,
        ReadingStatus status,
        LocalDateTime addedAt
) {}