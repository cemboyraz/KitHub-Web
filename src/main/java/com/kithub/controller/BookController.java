package com.kithub.controller;

import com.kithub.dto.GoogleBooksResponse;
import com.kithub.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping("/search")
    public ResponseEntity<GoogleBooksResponse> searchBooks(@RequestParam String query) {
        GoogleBooksResponse response = bookService.searchBooksFromGoogle(query);
        return ResponseEntity.ok(response);
    }
}