package com.kithub.service;

import com.kithub.dto.GoogleBooksResponse;
import com.kithub.model.Book;
import com.kithub.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final RestTemplate restTemplate;


    public GoogleBooksResponse searchBooksFromGoogle(String query) {
        String url = "https://www.googleapis.com/books/v1/volumes?q=" + query;

        // RestTemplate, gelen JSON'u senin o harika iç içe DTO'na otomatik map'ler!
        return restTemplate.getForObject(url, GoogleBooksResponse.class);
    }

    public Book saveBookIfNotExists(String googleId, String title, String author, String summary, String imageUrl, String category) {

        Optional<Book> existingBook = bookRepository.findByGoogleBooksId(googleId);
        if (existingBook.isPresent()) {
            return existingBook.get();
        }

        Book newBook = new Book();
        newBook.setGoogleBooksId(googleId);
        newBook.setTitle(title);
        newBook.setAuthor(author);

        if (summary != null && summary.length() > 1900) {
            newBook.setSummary(summary.substring(0, 1900) + "...");
        } else {
            newBook.setSummary(summary);
        }

        newBook.setImageUrl(imageUrl);
        newBook.setCategory(category);

        return bookRepository.save(newBook);
    }
}