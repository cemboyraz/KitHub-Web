package com.kithub.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // API olduğunu belirtir
@RequestMapping("/api/recommendation") // Temel URL: localhost:8080/api/books
@RequiredArgsConstructor
public class AIRecommendationController {
}
