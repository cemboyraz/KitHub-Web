package com.kithub.controller;

import com.kithub.dto.GoogleBooksResponse;
import com.kithub.model.Book;
import com.kithub.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    // Anasayfa için kitapları getiren endpoint
    @GetMapping("/home")
    public ResponseEntity<List<Book>> getHomeBooks() {
        return ResponseEntity.ok(bookService.getPopularBooks());
    }

    // köprü endpoinbtı
    @GetMapping("/details/{googleId}")
    public ResponseEntity<Book> getBookDetails(@PathVariable String googleId) {
        Book book = bookService.getBookDetails(googleId);
        return ResponseEntity.ok(book);
    }


    // kitap arama

    @GetMapping("/search")
    public ResponseEntity<GoogleBooksResponse> searchBooks(@RequestParam String query) {
        return ResponseEntity.ok(bookService.searchBooksFromGoogle(query));
    }
}