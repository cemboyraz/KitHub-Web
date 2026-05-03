package com.kithub.controller;

import com.kithub.dto.UserResponse;
import com.kithub.model.AIRecommendation;
import com.kithub.model.UserBook;
import com.kithub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserResponse> getUserProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    @GetMapping("/{userId}/library")
    public ResponseEntity<List<UserBook>> getUserLibrary(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserLibrary(userId));
    }

    @GetMapping("/getusers")
    public ResponseEntity<List>getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{userId}/recommendations")
    public ResponseEntity<List<AIRecommendation>> getUserRecommendations(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserRecommendations(userId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{userId}/ban")
    public ResponseEntity<String> banUser(@PathVariable Long userId) {
        String result = userService.banUser(userId);
        return ResponseEntity.ok(result);
    }
}