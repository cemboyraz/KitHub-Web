package com.kithub.controller;

import com.kithub.model.AIRecommendation;
import com.kithub.service.AIRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIRecommendationController {

    private final AIRecommendationService aiRecommendationService;

    @PostMapping("/recommend/{userId}")
    public ResponseEntity<AIRecommendation> getRecommendation(@PathVariable Long userId) {

        // Servisteki o harika kota kontrollü ve cold-start korumalı metodumuzu çağırıyoruz
        AIRecommendation recommendation = aiRecommendationService.generateRecommendationForUser(userId);

        return ResponseEntity.ok(recommendation);
    }
}