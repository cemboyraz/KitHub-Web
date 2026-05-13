package com.kithub.dto;

import java.time.LocalDateTime;

// Frontend'e gidince patlamasın diye
public record CommentResponse(
        Long id,
        String text,
        int starCount,
        String username,
        LocalDateTime createdAt
) {}