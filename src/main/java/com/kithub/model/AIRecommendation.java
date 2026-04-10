package com.kithub.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "recommendations")
@Data
public class AIRecommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Book book;

    private Double matchScore; // AI'nın tahmin başarısı (Örn: 0.95)
    private String recommendationSource; // "SimiliarBooks" veya "BasedOnFavoriteAuthor"
    private LocalDateTime generatedAt = LocalDateTime.now();
}
