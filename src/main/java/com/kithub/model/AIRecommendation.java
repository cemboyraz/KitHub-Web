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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    private Double matchScore;

    private String recommendationSource;

    @Column(length = 1000)
    private String aiReasoning; // Gemini'nin açıklaması

    private LocalDateTime generatedAt = LocalDateTime.now();
}