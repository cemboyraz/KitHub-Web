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
        List<String> finishedBooks =
                userBookService.getUserFinishedBooksForAI(user.getId());

        if (finishedBooks == null || finishedBooks.isEmpty()) {
            throw new RuntimeException("En az 1 kitap bitirmelisiniz");
        }

        // ================= PROMPT =================
        String prompt =
                "Bitirdiğim kitaplar: " + String.join(", ", finishedBooks) +
                        ". Bana 1 kitap öner. SADECE JSON döndür: " +
                        "{title, author, matchScore, reasoning}";

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
                geminiResponse.candidates().isEmpty() ||
                geminiResponse.candidates().get(0).content() == null) {
            throw new RuntimeException("Gemini boş response döndü");
        }

        try {

            // ================= AI TEXT =================
            String aiText = geminiResponse.candidates()
                    .get(0)
                    .content()
                    .parts()
                    .get(0)
                    .text();

            if (aiText == null || aiText.isBlank()) {
                throw new RuntimeException("AI response boş");
            }

            aiText = aiText
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            JsonNode jsonNode;
            try {
                jsonNode = objectMapper.readTree(aiText);
            } catch (Exception e) {
                throw new RuntimeException("AI JSON parse edilemedi: " + aiText);
            }

            String title = jsonNode.path("title").asText(null);
            String author = jsonNode.path("author").asText(null);
            double matchScore = jsonNode.path("matchScore").asDouble(0.0);
            String reasoning = jsonNode.path("reasoning").asText("");

            if (title == null || author == null) {
                throw new RuntimeException("AI eksik veri döndü");
            }

            // ================= GOOGLE SEARCH =================
            String searchQuery = title + " " + author;

            var googleResult = bookService.searchBooksFromGoogle(searchQuery);

            if (googleResult == null ||
                    googleResult.items() == null ||
                    googleResult.items().isEmpty()) {
                throw new RuntimeException("Google Books sonuç yok: " + title);
            }

            var bestItem = googleResult.items().get(0);
            var volInfo = bestItem.volumeInfo();

            if (volInfo == null) {
                throw new RuntimeException("VolumeInfo null geldi");
            }

            // ================= SAFE FIELDS =================
            String imageUrl = (volInfo.imageLinks() != null)
                    ? volInfo.imageLinks().thumbnail()
                    : null;

            String category = (volInfo.categories() != null && !volInfo.categories().isEmpty())
                    ? volInfo.categories().get(0)
                    : "Unknown";

            String safeAuthor = (volInfo.authors() != null && !volInfo.authors().isEmpty())
                    ? volInfo.authors().get(0)
                    : author;

            // ================= SAVE BOOK =================
            Book savedBook = bookService.saveBookIfNotExists(
                    bestItem.id(),
                    volInfo.title(),
                    safeAuthor,
                    volInfo.description(),
                    imageUrl,
                    category
            );

            // ================= SAVE AI RECOMMENDATION =================
            AIRecommendation recommendation = new AIRecommendation();
            recommendation.setUser(user);
            recommendation.setBook(savedBook);
            recommendation.setMatchScore(matchScore);
            recommendation.setAiReasoning(reasoning);
            recommendation.setRecommendationSource("GEMINI_1.5_FLASH");

            aiRecommendationRepository.save(recommendation);

            // ================= RESPONSE =================
            BookResponse bookResponse = new BookResponse(
                    savedBook.getId(),
                    savedBook.getGoogleBooksId(),
                    savedBook.getTitle(),
                    savedBook.getAuthor(),
                    savedBook.getImageUrl(),
                    savedBook.getCategory(),
                    0.0f
            );

            return new AIRecommendationResponse(
                    recommendation.getId(),
                    bookResponse,
                    matchScore,
                    reasoning
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "AI recommendation error: " + e.getMessage(), e
            );
        }
    }
}