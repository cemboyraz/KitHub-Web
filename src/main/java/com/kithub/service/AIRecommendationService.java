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
    private final ObjectMapper objectMapper; // JSON Parse etmek için

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Transactional
    public AIRecommendationResponse generateRecommendationForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));

        // Günlük Kota Kontrolü
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long requestCountToday = aiRecommendationRepository.countByUserAndGeneratedAtAfter(user, startOfDay);

        if (requestCountToday >= 1) {
            throw new RuntimeException("Günlük yapay zeka öneri kotanızı (1) doldurdunuz. Lütfen yarın tekrar deneyin.");
        }

        // Bitirilmiş Kitapları Çek
        List<String> finishedBooks = userBookService.getUserFinishedBooksForAI(user.getId());
        if (finishedBooks.isEmpty()) {
            throw new RuntimeException("Öneri yapabilmemiz için önce kütüphanenize bitirdiğiniz birkaç kitap eklemelisiniz.");
        }

        // 1. AŞAMA: GEMINI API'YE YALVARMADAN JSON İSTEMEK
        String prompt = "Benim bitirdiğim kitaplar şunlar: " + String.join(", ", finishedBooks) + ". "
                + "Lütfen bana bu tarzda çok seveceğim YENİ 1 TANE kitap öner. "
                + "Cevabını SADECE VE SADECE aşağıdaki JSON formatında ver. Markdown kullanma, başına sonuna açıklama ekleme:\n"
                + "{\n"
                + "  \"title\": \"Kitabın Orijinal Adı\",\n"
                + "  \"author\": \"Yazarın Adı\",\n"
                + "  \"matchScore\": 95.5,\n"
                + "  \"reasoning\": \"Bu kitabı neden önerdiğine dair 2 cümlelik açıklaman (Türkçe)\"\n"
                + "}";

        GeminiRequest requestBody = new GeminiRequest(List.of(new GeminiRequest.Content(List.of(new GeminiRequest.Part(prompt)))));
        String fullUrl = geminiApiUrl + geminiApiKey;
        GeminiResponse geminiResponse = restTemplate.postForObject(fullUrl, requestBody, GeminiResponse.class);

        if (geminiResponse == null || geminiResponse.candidates().isEmpty()) {
            throw new RuntimeException("Yapay Zekadan cevap alınamadı.");
        }

        try {
            // Gemini'nin döndüğü JSON metnini temizle (bazen ```json tag'i koyabiliyor)
            String aiAnswerText = geminiResponse.candidates().get(0).content().parts().get(0).text();
            aiAnswerText = aiAnswerText.replace("```json", "").replace("```", "").trim();


                    JsonNode jsonNode = objectMapper.readTree(aiAnswerText);
            String title = jsonNode.get("title").asText();
            String author = jsonNode.get("author").asText();
            Double matchScore = jsonNode.get("matchScore").asDouble();
            String reasoning = jsonNode.get("reasoning").asText();

            // 2. AŞAMA: GOOGLE BOOKS API'DEN GERÇEK KİTAP BİLGİLERİNİ ÇEK VE KAYDET
            String searchQuery = title + " " + author;
            var googleResult = bookService.searchBooksFromGoogle(searchQuery);

            if (googleResult.items() == null || googleResult.items().isEmpty()) {
                throw new RuntimeException("AI bir kitap önerdi ama sistemde bulunamadı: " + title);
            }

            // İlk sonucu alıp DB'ye kaydediyoruz (hayalet kitap mantığı)
            var bestItem = googleResult.items().get(0);
            var volInfo = bestItem.volumeInfo();
            String imageUrl = volInfo.imageLinks() != null ? volInfo.imageLinks().thumbnail() : null;
            String category = volInfo.categories() != null && !volInfo.categories().isEmpty() ? volInfo.categories().get(0) : "Unknown";

            Book savedBook = bookService.saveBookIfNotExists(
                    bestItem.id(),
                    volInfo.title(),
                    volInfo.authors() != null ? volInfo.authors().get(0) : author,
                    volInfo.description(),
                    imageUrl,
                    category
            );

            // 3. AŞAMA: TAVSİYEYİ KAYDET VE DTO DÖN
            AIRecommendation recommendation = new AIRecommendation();
            recommendation.setUser(user);
            recommendation.setBook(savedBook);
            recommendation.setMatchScore(matchScore);
            recommendation.setAiReasoning(reasoning);
            recommendation.setRecommendationSource("GEMINI_1.5_FLASH");

            aiRecommendationRepository.save(recommendation);

            //  DTO formatında geriye dönüyoruz
            BookResponse bookResponse = new BookResponse(
                    savedBook.getId(), savedBook.getGoogleBooksId(), savedBook.getTitle(),
                    savedBook.getAuthor(), savedBook.getImageUrl(), savedBook.getCategory(), 0.0f
            );

            return new AIRecommendationResponse(recommendation.getId(), bookResponse, matchScore, reasoning);

        } catch (Exception e) {
            throw new RuntimeException("Yapay Zeka önerisi işlenirken hata oluştu: " + e.getMessage());
        }
    }
}