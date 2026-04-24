package com.kithub.dto;

import com.kithub.model.ReadingStatus;
import jakarta.validation.constraints.NotNull;

public record UserBookRequest(
        @NotNull(message = "Google Kitap ID boş olamaz!")
        String googleBooksId,

        @NotNull(message = "Okuma durumu boş olamaz!")
        ReadingStatus status
) {}