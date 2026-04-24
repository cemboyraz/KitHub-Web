package com.kithub.dto;

public record CommentRequest(
        Long bookId,
        String text
) {}