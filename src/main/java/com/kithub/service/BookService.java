package com.kithub.service;

import com.kithub.dto.GoogleBooksResponse;
import com.kithub.model.Book;
import com.kithub.model.User;
import com.kithub.model.UserBook;
import com.kithub.repository.BookRepository;
import com.kithub.repository.UserBookRepository;
import com.kithub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final RestTemplate restTemplate;


    @Value("${google.books.api.key}")
    private String apiKey;


    // GOOGLE BOOKS araması+key ekledik ve ara ara değişmek gerektirebiliyor

    public GoogleBooksResponse searchBooksFromGoogle(String query) {

        String encodedQuery =
                URLEncoder.encode(query, StandardCharsets.UTF_8);

        String url =
                "https://www.googleapis.com/books/v1/volumes?q="
                        + encodedQuery
                        + "&maxResults=20"
                        + "&orderBy=relevance"
                        + "&printType=books"
                        + "&key="
                        + apiKey;

        System.out.println("Google Books URL: " + url);

        try {
            GoogleBooksResponse response =
                    restTemplate.getForObject(url, GoogleBooksResponse.class);

            return response;
        } catch (Exception e) {
            System.out.println("Google Books API ERROR: " + e.getMessage());
            return new GoogleBooksResponse(java.util.List.of());
        }
    }

    // metot isminden belli eğer kitap yoksa kaydetme.

    public Book saveBookIfNotExists(
            String googleId,
            String title,
            String author,
            String summary,
            String imageUrl,
            String category
    ) {

        Optional<Book> existingBook =
                bookRepository.findByGoogleBooksId(googleId);

        if (existingBook.isPresent()) {
            return existingBook.get();
        }

        Book newBook = new Book();

        newBook.setGoogleBooksId(googleId);

        newBook.setTitle(
                title != null ? title : "Unknown Title"
        );

        newBook.setAuthor(
                author != null ? author : "Unknown Author"
        );

        newBook.setSummary(
                summary != null && summary.length() > 1900
                        ? summary.substring(0, 1900) + "..."
                        : (summary != null ? summary : "No description")
        );

        newBook.setImageUrl(
                imageUrl != null ? imageUrl : ""
        );

        newBook.setCategory(
                category != null ? category : "Unknown"
        );

        return bookRepository.save(newBook);
    }
    //favori kitap kaydetme
    public Book saveFavoriteBook(
            Long userId,
            String googleId,
            String title,
            String author,
            String summary,
            String imageUrl,
            String category
    ) {

        Optional<Book> existing =
                bookRepository.findByGoogleBooksIdAndUserId(
                        googleId,
                        userId
                );

        if (existing.isPresent()) {
            return existing.get();
        }

        Book book = new Book();

        book.setGoogleBooksId(googleId);
        book.setTitle(title != null ? title : "Unknown Title");
        book.setAuthor(author != null ? author : "Unknown Author");

        book.setSummary(
                summary != null && summary.length() > 1900
                        ? summary.substring(0, 1900) + "..."
                        : (summary != null ? summary : "No description")
        );

        book.setImageUrl(imageUrl != null ? imageUrl : "");
        book.setCategory(category != null ? category : "Unknown");

        book.setUserId(userId);

        return bookRepository.save(book);
    }

    public Book getBookById(Long id) {

        return bookRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Kitap bulunamadı!")
                );
    }

    public GoogleBooksResponse getPopularBooks() {

        String url =
                "https://www.googleapis.com/books/v1/volumes?q=java"
                        + "&maxResults=20";

        try {
            GoogleBooksResponse response =
                    restTemplate.getForObject(url, GoogleBooksResponse.class);

            // null safeguard
            if (response == null || response.items() == null) {
                return new GoogleBooksResponse(java.util.List.of());
            }

            return response;

        } catch (Exception e) {
            System.out.println("Google Books ERROR: " + e.getMessage());

            return new GoogleBooksResponse(java.util.List.of());
        }
    }
}