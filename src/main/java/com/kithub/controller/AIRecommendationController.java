package com.kithub.controller;

import com.kithub.dto.AIRecommendationResponse;
import com.kithub.service.AIRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIRecommendationController {

    private final AIRecommendationService aiRecommendationService;

    // PathVariable silindi, IDOR açığı kapatıldı .Veri direkt JWT'den geliyor.
    @PostMapping("/recommend")
    public ResponseEntity<AIRecommendationResponse> getRecommendation(@AuthenticationPrincipal UserDetails userDetails) {
        AIRecommendationResponse response = aiRecommendationService.generateRecommendationForUser(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}