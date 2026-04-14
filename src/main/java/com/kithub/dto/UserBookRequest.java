package com.kithub.dto;

import com.kithub.model.ReadingStatus;

public record UserBookRequest(
        Long bookId,
        ReadingStatus status
) {}