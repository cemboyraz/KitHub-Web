package com.kithub.controller;

import com.kithub.dto.UserResponse;
import com.kithub.model.AIRecommendation;
import com.kithub.model.User;
import com.kithub.model.UserBook;
import com.kithub.repository.UserRepository; // Bunu ekledik
import com.kithub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/me/profile")
    public ResponseEntity<UserResponse> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        return ResponseEntity.ok(userService.getUserProfile(user.getId()));
    }

    // listemi getir
    @GetMapping("/me/library")
    public ResponseEntity<List<UserBook>> getMyLibrary(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        return ResponseEntity.ok(userService.getUserLibrary(user.getId()));
    }

    @GetMapping("/me/recommendations")
    public ResponseEntity<List<AIRecommendation>> getMyRecommendations(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        return ResponseEntity.ok(userService.getUserRecommendations(user.getId()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{userId}/ban")
    public ResponseEntity<String> banUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.banUser(userId));
    }
}