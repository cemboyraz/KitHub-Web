package com.kithub.repository;

import com.kithub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    //
    Optional<User> findByUser(String username);

    // Aynı kullanıcı adından 2 tane olmasın diye
    boolean existsByUsername(String username);

    //  adamı mailinden bul
    Optional<User> findByEmail(String email);

    // Bu mail zaten kullanılıyor hatası için
    boolean existsByEmail(String email);
}