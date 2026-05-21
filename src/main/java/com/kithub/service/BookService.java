package com.kithub.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper; // KANKA: Bunu ekledik ki JSON'ı güvenle parçalayabilelim
import com.kithub.dto.GoogleBooksResponse;
import com.kithub.model.Book;
import com.kithub.repository.BookRepository;
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

    // Google Books Toplu Arama
    public GoogleBooksResponse searchBooksFromGoogle(String query) {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

        String url = "https://www.googleapis.com/books/v1/volumes?q="
                + encodedQuery
                + "&maxResults=20"
                + "&orderBy=relevance"
                + "&printType=books"
                + "&key=" + apiKey;

        try {
            return restTemplate.getForObject(url, GoogleBooksResponse.class);
        } catch (Exception e) {
            System.out.println("Google Books API ERROR: " + e.getMessage());
            return new GoogleBooksResponse(java.util.List.of());
        }
    }

    public Book saveBookIfNotExists(String googleId, String title, String author, String summary, String imageUrl, String category) {
        return bookRepository.findById(googleId)
                .orElseGet(() -> {
                    Book book = new Book();
                    book.setId(googleId);
                    book.setTitle(title != null ? title : "Unknown Title");
                    book.setAuthor(author != null ? author : "Unknown Author");

                    if (summary != null && summary.length() > 1900) {
                        book.setSummary(summary.substring(0, 1900) + "...");
                    } else {
                        book.setSummary(summary != null ? summary : "No description");
                    }

                    book.setImageUrl(imageUrl != null ? imageUrl : "");
                    book.setCategory(category != null ? category : "Unknown");

                    return bookRepository.save(book);
                });
    }

    public Book getBookById(String id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kitap bulunamadı!"));
    }

    public List<Book> getPopularBooks() {
        return bookRepository.findRandomBooks();
    }


    // TWO World problem

    public Book getBookDetails(String googleId) {
        // 1. Önce kendi KitHub veritabanımıza bakıyoruz (Yorumlar ve Puanlar burada var)
        Optional<Book> localBook = bookRepository.findById(googleId);

        if (localBook.isPresent()) {
            // Kitap bizde varsa, direkt onu dönüyoruz. İçindeki yorumlar jilet gibi gelecek.
            return localBook.get();
        }

        // 2. Kitap bizde YOKSA, Google API'den çekiyoruz
        String url = "https://www.googleapis.com/books/v1/volumes/" + googleId + "?key=" + apiKey;

        try {
            // 🔥 KANKA BÜYÜ BURADA: Veriyi JsonNode diye zorlamak yerine önce dümdüz String (Metin) olarak alıyoruz
            String jsonResponse = restTemplate.getForObject(url, String.class);

            if (jsonResponse == null) {
                throw new RuntimeException("Google Books API'den boş yanıt geldi.");
            }

            // Sonra kendi kalkanımızla (ObjectMapper) onu json'a çeviriyoruz, böylece asla çökmüyor!
            ObjectMapper mapper = new ObjectMapper();
            JsonNode response = mapper.readTree(jsonResponse);

            if (!response.has("volumeInfo")) {
                throw new RuntimeException("Google Books API'den kitap bulunamadı.");
            }

            JsonNode volumeInfo = response.get("volumeInfo");
            Book book = new Book();
            book.setId(googleId);

            book.setTitle(volumeInfo.has("title") ? volumeInfo.get("title").asText() : "Unknown Title");

            if (volumeInfo.has("authors") && volumeInfo.get("authors").isArray()) {
                book.setAuthor(volumeInfo.get("authors").get(0).asText());
            } else {
                book.setAuthor("Unknown Author");
            }

            if (volumeInfo.has("description")) {
                String summary = volumeInfo.get("description").asText();
                book.setSummary(summary.length() > 1900 ? summary.substring(0, 1900) + "..." : summary);
            } else {
                book.setSummary("No description available.");
            }

            if (volumeInfo.has("imageLinks") && volumeInfo.get("imageLinks").has("thumbnail")) {
                book.setImageUrl(volumeInfo.get("imageLinks").get("thumbnail").asText());
            } else {
                book.setImageUrl("");
            }

            if (volumeInfo.has("categories") && volumeInfo.get("categories").isArray()) {
                book.setCategory(volumeInfo.get("categories").get(0).asText());
            } else {
                book.setCategory("Unknown");
            }

            return book;

        } catch (Exception e) {
            System.out.println("Google API'den detay çekilirken hata: " + e.getMessage());
            throw new RuntimeException("Kitap detayları çekilemedi.");
        }
    }
}