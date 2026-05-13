package com.kithub.controller;

import com.kithub.dto.UserBookRequest;
import com.kithub.dto.UserBookResponse;
import com.kithub.model.UserBook;
import com.kithub.service.UserBookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
public class UserBookController {

    private final UserBookService userBookService;

    //  Başkasının adına kitap eklenemez.
    @PostMapping("/me/add")
    public ResponseEntity<String> addOrUpdateLibrary(
            @AuthenticationPrincipal UserDetails userDetails, // bu notasyon bir kullanıcı başkalarının tokeniyle iş yapamasın diye
            @Valid @RequestBody UserBookRequest request) {

        UserBook userBook = userBookService.addOrUpdateLibrary(
                userDetails.getUsername(),
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

    @GetMapping("/me")
    public ResponseEntity<List<UserBookResponse>> getMyLibrary(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userBookService.getUserLibrary(userDetails.getUsername()));
    }
}