package com.kithub.dto;

// Yorum güncellerken body'den gelecek veri
public record UpdateCommentRequest(
        String text,
        Integer starCount
) {}