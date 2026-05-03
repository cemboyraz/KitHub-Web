package com.kithub.service;

import com.kithub.dto.LoginRequest; // Yeni eklediğimiz DTO
import com.kithub.dto.RegisterRequest;
import com.kithub.model.Role;
import com.kithub.model.User;
import com.kithub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService; // Token üretmek için bunu ekledik

    public String register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Hata: Bu email zaten kullanımda!");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);

        userRepository.save(user);
        return "Aramıza hoş geldin, " + user.getUsername() + "! Kayıt başarıyla tamamlandı.";
    }

    public String login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Hata: Email veya şifre yanlış!"));

        //Veritabanındaki hash ile kullanıcının girdiğini karşılaştırır
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Hata: Email veya şifre yanlış!");
        }

        return jwtService.generateToken(user.getEmail());
    }
}