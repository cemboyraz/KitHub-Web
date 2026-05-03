package com.kithub.controller;

import com.kithub.dto.UserBookRequest; // DTO'muz burada!
import com.kithub.model.UserBook;
import com.kithub.service.UserBookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
public class UserBookController {

    private final UserBookService userBookService;

    @PostMapping("/user/{userId}/add")
    public ResponseEntity<String> addOrUpdateLibrary(
            @PathVariable Long userId,
            @Valid @RequestBody UserBookRequest request) {

        // Servise DTO'nun içindeki parçaları tek tek postalama
        UserBook userBook = userBookService.addOrUpdateLibrary(
                userId,
                request.googleBooksId(),
                request.title(),
                request.author(),
                request.summary(),
                request.imageUrl(),
                request.category(),
                request.status()
        );

        return ResponseEntity.ok("Kitap kütüphanenize başarıyla eklendi/güncellendi. Yeni Statü: " + userBook.getStatus());
    }
}