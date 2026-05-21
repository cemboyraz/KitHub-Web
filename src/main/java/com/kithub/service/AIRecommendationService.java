package com.kithub.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kithub.dto.AIRecommendationResponse;
import com.kithub.dto.BookResponse;
import com.kithub.dto.GeminiRequest;
import com.kithub.dto.GeminiResponse;
import com.kithub.model.AIRecommendation;
import com.kithub.model.Book;
import com.kithub.model.User;
import com.kithub.repository.AIRecommendationRepository;
import com.kithub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AIRecommendationService {

    private final UserRepository userRepository;
    private final AIRecommendationRepository aiRecommendationRepository;
    private final UserBookService userBookService;
    private final BookService bookService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Transactional
    public AIRecommendationResponse generateRecommendationForUser(String email) {

        // ================= USER =================
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        // ================= DAILY LIMIT =================
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

        long requestCountToday =
                aiRecommendationRepository.countByUserAndGeneratedAtAfter(user, startOfDay);

        if (requestCountToday >= 1) {
            throw new RuntimeException("Günlük AI öneri limitine ulaştınız");
        }

        // ================= BOOK HISTORY =================
        // Burada userId (Long) kullanıyoruz, sıkıntı yok
        List<String> finishedBooks =
                userBookService.getUserFinishedBooksForAI(user.getId());

        if (finishedBooks == null || finishedBooks.isEmpty()) {
            throw new RuntimeException("En az 1 kitap bitirmelisiniz");
        }

        // ================= PROMPT =================
        String prompt =
                "Bitirdiğim kitaplar: " + String.join(", ", finishedBooks) +
                        ". Bana 1 kitap öner. SADECE JSON döndür: " +
                        "{ \"title\": \"...\", \"author\": \"...\", \"matchScore\": 95, \"reasoning\": \"...\" }";

        GeminiRequest requestBody =
                new GeminiRequest(List.of(
                        new GeminiRequest.Content(
                                List.of(new GeminiRequest.Part(prompt))
                        )
                ));

        String fullUrl = geminiApiUrl + geminiApiKey;

        GeminiResponse geminiResponse;
        try {
            geminiResponse = restTemplate.postForObject(fullUrl, requestBody, GeminiResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Gemini API hatası: " + e.getMessage());
        }

        if (geminiResponse == null ||
                geminiResponse.candidates() == null ||
                geminiResponse.candidates().isEmpty()) {
            throw new RuntimeException("Gemini boş response döndü");
        }

        try {
            // ================= AI TEXT PARSING =================
            String aiText = geminiResponse.candidates().get(0).content().parts().get(0).text();
            aiText = aiText.replace("```json", "").replace("```", "").trim();

            JsonNode jsonNode = objectMapper.readTree(aiText);

            String title = jsonNode.path("title").asText(null);
            String author = jsonNode.path("author").asText(null);
            double matchScore = jsonNode.path("matchScore").asDouble(0.0);
            String reasoning = jsonNode.path("reasoning").asText("");

            // ================= GOOGLE SEARCH =================
            var googleResult = bookService.searchBooksFromGoogle(title + " " + author);

            if (googleResult == null || googleResult.items() == null || googleResult.items().isEmpty()) {
                throw new RuntimeException("Google Books sonuç yok: " + title);
            }

            var bestItem = googleResult.items().get(0);
            var volInfo = bestItem.volumeInfo();

            // ================= SAVE BOOK (MİMARİYE UYGUN) =================
            Book savedBook = bookService.saveBookIfNotExists(
                    bestItem.id(), // Artik bu direkt savedBook.getId() (String)
                    volInfo.title(),
                    (volInfo.authors() != null) ? volInfo.authors().get(0) : author,
                    volInfo.description(),
                    (volInfo.imageLinks() != null) ? volInfo.imageLinks().thumbnail() : null,
                    (volInfo.categories() != null) ? volInfo.categories().get(0) : "General"
            );

            // ================= SAVE RECOMMENDATION =================
            AIRecommendation recommendation = new AIRecommendation();
            recommendation.setUser(user);
            recommendation.setBook(savedBook);
            recommendation.setMatchScore(matchScore);
            recommendation.setAiReasoning(reasoning);
            recommendation.setRecommendationSource("GEMINI_1.5_FLASH");

            aiRecommendationRepository.save(recommendation);

            // ================= RESPONSE (DÜZELTİLEN YER) =================
            // getGoogleBooksId() yerine getId() kullanıyoruz
            // AIRecommendationService.java içinde (satır 139 civarı)
            BookResponse bookResponse = new BookResponse(
                    savedBook.getId(),
                    savedBook.getTitle(),
                    savedBook.getAuthor(),
                    savedBook.getImageUrl(),
                    savedBook.getCategory(),
                    0.0f // averageRating için varsayılan değer
            );

            return new AIRecommendationResponse(
                    recommendation.getId(),
                    bookResponse,
                    matchScore,
                    reasoning
            );

        } catch (Exception e) {
            throw new RuntimeException("AI recommendation error: " + e.getMessage());
        }
    }
}