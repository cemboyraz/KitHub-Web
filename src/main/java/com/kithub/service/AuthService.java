package com.kithub.service;

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

    public String register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Hata: Bu email zaten kullanımda!");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new RuntimeException("Hata: Bu kullanıcı adı zaten alınmış!");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());

        // encoder ile şifreliyoruz
        user.setPassword(passwordEncoder.encode(request.password()));

        user.setRole(Role.USER);

        userRepository.save(user);

        return "Aramıza hoş geldin, " + user.getUsername() + "! Kayıt başarıyla tamamlandı.";
    }
}
