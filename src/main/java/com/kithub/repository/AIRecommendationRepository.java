package com.kithub.repository;

import com.kithub.model.AIRecommendation;
import com.kithub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AIRecommendationRepository extends JpaRepository<AIRecommendation, Long> {

    // KOTA KURALI: Kullanıcı gece 00:00'dan sonra kaç kez öneri almış?
    // Eğer bu sayı 1 ise, Service katmanında "Hata" fırlatacağız.
    long countByUserAndGeneratedAtAfter(User user, LocalDateTime startOfDay);

    // Bu listeyi çekip, içindeki kitapları Gemini'ye bunları önermee diyeceğiz.
    List<AIRecommendation> findByUser(User user);

    Optional<AIRecommendation> findFirstByUserOrderByGeneratedAtDesc(User user);
}