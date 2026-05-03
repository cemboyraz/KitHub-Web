package com.kithub.dto;

import com.kithub.model.ReadingStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserBookRequest(
        @NotBlank(message = "Google Kitap ID boş olamaz!")
        String googleBooksId,

        @NotBlank(message = "Kitap adı boş olamaz!")
        String title,

        String author,
        String summary,
        String imageUrl,
        String category,

        @NotNull(message = "Okuma durumu boş olamaz!")
        ReadingStatus status
) {}