package com.kithub.service;

import com.kithub.model.AIRecommendation;
import com.kithub.model.Book;
import com.kithub.model.User;
import com.kithub.repository.AIRecommendationRepository;
import com.kithub.repository.BookRepository;
import com.kithub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AIRecommendationService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final AIRecommendationRepository aiRecommendationRepository;
    private final UserBookService userBookService;

    @Transactional
    public AIRecommendation generateRecommendationForUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long requestCountToday = aiRecommendationRepository.countByUserAndGeneratedAtAfter(user, startOfDay);

        if (requestCountToday >= 1) {
            throw new RuntimeException("Günlük yapay zeka öneri kotanızı (1) doldurdunuz. Lütfen yarın tekrar deneyin.");
        }

        List<String> finishedBooks = userBookService.getUserFinishedBooksForAI(userId);

        if (finishedBooks.isEmpty()) {
            throw new RuntimeException("Öneri yapabilmemiz için önce kütüphanenize bitirdiğiniz birkaç kitap eklemelisiniz.");
        }

        String recommendedBookGoogleId = "12345ABCDE"; // Örnek Google ID
        String recommendedTitle = "Otostopçunun Galaksi Rehberi";
        String aiReasoning = "Bilim kurgu türündeki okumalarınızı çok sevdiğinizi gördüm. Bu kitap tam size göre!";
        Double matchScore = 95.5;

        Book recommendedBook = bookRepository.findByGoogleBooksId(recommendedBookGoogleId)
                .orElseGet(() -> {
                    Book newBook = new Book();
                    newBook.setGoogleBooksId(recommendedBookGoogleId);
                    newBook.setTitle(recommendedTitle);
                    return bookRepository.save(newBook);
                });

        AIRecommendation recommendation = new AIRecommendation();
        recommendation.setUser(user);
        recommendation.setBook(recommendedBook);
        recommendation.setMatchScore(matchScore);
        recommendation.setAiReasoning(aiReasoning);
        recommendation.setRecommendationSource("GEMINI_PRO");

        return aiRecommendationRepository.save(recommendation);
    }
}