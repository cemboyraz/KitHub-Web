package com.kithub.service;

import com.kithub.dto.UserResponse;
import com.kithub.model.AIRecommendation;
import com.kithub.model.User;
import com.kithub.model.UserBook;
import com.kithub.repository.AIRecommendationRepository;
import com.kithub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AIRecommendationRepository aiRecommendationRepository;

    public UserResponse getUserProfile(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }

    @Transactional(readOnly = true)
    public List<UserBook> getUserLibrary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));

        return user.getUserBooks();
    }
    
    public List<AIRecommendation> getUserRecommendations(Long userId) {
        return aiRecommendationRepository.findByUserId(userId);
    }

    @Transactional
    public String banUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));

        user.setBlacklisted(true);
        userRepository.save(user);

        return user.getUsername() + " başarıyla banlandı. Artık işlem yapamaz.";
    }
}