package com.kithub.repository;

import com.kithub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Senin eklediğin: Kullanıcı adıyla bulmak için
    Optional<User> findByUsername(String username);

    // Aynı kullanıcı adından 2 tane olmasın diye kontrol
    boolean existsByUsername(String username);

    // 1. (Kayıt/Giriş) için adamı mailinden bul
    Optional<User> findByEmail(String email);

    // 2. Kayıt olurken "Bu mail zaten kullanılıyor" hatası fırlatmak için
    boolean existsByEmail(String email);
}