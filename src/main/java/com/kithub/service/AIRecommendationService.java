package com.kithub.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kithub.dto.*;
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

    // GEMINI CONFIG
    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    // OLLAMA CONFIG
    @Value("${ollama.api.url}")
    private String ollamaApiUrl;

    @Value("${ollama.model}")
    private String ollamaModel;

    @Transactional
    public AIRecommendationResponse generateRecommendationForUser(String email) {

        // ================= 1. USER & LIMIT KONTROLÜ =================
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long requestCountToday = aiRecommendationRepository.countByUserAndGeneratedAtAfter(user, startOfDay);

        if (requestCountToday >= 1) {
            throw new RuntimeException("Günlük AI öneri limitine ulaştınız");
        }

        // ================= 2. KİTAP GEÇMİŞİNİ ÇEKME =================
        List<String> finishedBooks = userBookService.getUserFinishedBooksForAI(user.getId());

        if (finishedBooks == null || finishedBooks.isEmpty()) {
            throw new RuntimeException("En az 1 kitap bitirmelisiniz");
        }

        String rawBookList = String.join(", ", finishedBooks);
        String userProfileSummary;

        // ================= 3. OLLAMA İLE ÖZETLEME (LOKAL LLM) =================
        try {
            String ollamaPrompt = "Aşağıdaki kitap listesini okuyan birinin okuma zevkini, sevdiği türleri ve temaları 2-3 cümleyle özetle. Sadece özeti ver. Kitaplar: " + rawBookList;

            OllamaRequest ollamaReq = new OllamaRequest(ollamaModel, ollamaPrompt, false);
            OllamaResponse ollamaRes = restTemplate.postForObject(ollamaApiUrl, ollamaReq, OllamaResponse.class);

            if (ollamaRes != null && ollamaRes.response() != null) {
                userProfileSummary = ollamaRes.response();
                System.out.println(" OLLAMA ÖZETİ BAŞARILI: " + userProfileSummary);
            } else {
                userProfileSummary = "Kullanıcı şu kitapları okudu: " + rawBookList;
            }
        } catch (Exception e) {
            // Ollama kapalıysa sistemi çökertme, Fallback olarak direkt listeyi kullan
            System.out.println("⚠ Ollama API'ye ulaşılamadı. Lokal özetleme atlanıyor. Hata: " + e.getMessage());
            userProfileSummary = "Kullanıcı şu kitapları okudu: " + rawBookList;
        }

        // ================= 4. GEMINI İLE KİTAP ÖNERİSİ ALMA =================
        String geminiPrompt =
                "Kullanıcının okuma zevki özeti: " + userProfileSummary +
                        ". Bu profile uygun 1 yepyeni kitap öner. SADECE JSON döndür: " +
                        "{ \"title\": \"...\", \"author\": \"...\", \"matchScore\": 95, \"reasoning\": \"...\" }";

        GeminiRequest requestBody = new GeminiRequest(List.of(
                new GeminiRequest.Content(List.of(new GeminiRequest.Part(geminiPrompt)))
        ));

        String fullUrl = geminiApiUrl + geminiApiKey;
        GeminiResponse geminiResponse;

        try {
            geminiResponse = restTemplate.postForObject(fullUrl, requestBody, GeminiResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Gemini API hatası: " + e.getMessage());
        }

        if (geminiResponse == null || geminiResponse.candidates() == null || geminiResponse.candidates().isEmpty()) {
            throw new RuntimeException("Gemini boş response döndü");
        }

        try {
            // ================= 5. JSON PARSING & DOĞRULAMA =================
            String aiText = geminiResponse.candidates().get(0).content().parts().get(0).text();
            aiText = aiText.replace("```json", "").replace("```", "").trim();

            JsonNode jsonNode = objectMapper.readTree(aiText);

            String title = jsonNode.path("title").asText(null);
            String author = jsonNode.path("author").asText(null);
            double matchScore = jsonNode.path("matchScore").asDouble(0.0);
            String reasoning = jsonNode.path("reasoning").asText("");

            // Google Books API ile kitabı doğrula
            var googleResult = bookService.searchBooksFromGoogle(title + " " + author);

            if (googleResult == null || googleResult.items() == null || googleResult.items().isEmpty()) {
                throw new RuntimeException("Google Books sonuç yok: " + title);
            }

            var bestItem = googleResult.items().get(0);
            var volInfo = bestItem.volumeInfo();

            Book savedBook = bookService.saveBookIfNotExists(
                    bestItem.id(),
                    volInfo.title(),
                    (volInfo.authors() != null) ? volInfo.authors().get(0) : author,
                    volInfo.description(),
                    (volInfo.imageLinks() != null) ? volInfo.imageLinks().thumbnail() : null,
                    (volInfo.categories() != null) ? volInfo.categories().get(0) : "General"
            );

            // ================= 6. SONUÇLARI KAYDET VE DÖNDÜR =================
            AIRecommendation recommendation = new AIRecommendation();
            recommendation.setUser(user);
            recommendation.setBook(savedBook);
            recommendation.setMatchScore(matchScore);
            recommendation.setAiReasoning(reasoning);
            recommendation.setRecommendationSource("OLLAMA_Llama3_PLUS_GEMINI_2.5");

            aiRecommendationRepository.save(recommendation);

            BookResponse bookResponse = new BookResponse(
                    savedBook.getId(),
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
            throw new RuntimeException("AI recommendation parsing error: " + e.getMessage());
        }
    }
}