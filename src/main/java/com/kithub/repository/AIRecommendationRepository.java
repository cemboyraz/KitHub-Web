package com.kithub.repository;

import com.kithub.model.AIRecommendation;
import com.kithub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AIRecommendationRepository extends JpaRepository<AIRecommendation, Long> {

    List<AIRecommendation> findByUserId(Long userId);

    long countByUserAndGeneratedAtAfter(User user, LocalDateTime startOfDay);

    // 3. Gemini'ye "bunları bir daha önerme" demek için kullanıcının geçmişini çekme
    List<AIRecommendation> findByUser(User user);

    Optional<AIRecommendation> findFirstByUserOrderByGeneratedAtDesc(User user);
}