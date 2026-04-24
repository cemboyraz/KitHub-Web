package com.kithub.repository;

import com.kithub.model.Book;
import com.kithub.model.ReadingStatus;
import com.kithub.model.User;
import com.kithub.model.UserBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBookRepository extends JpaRepository<UserBook, Long> {

    Optional<UserBook> findByUserAndBook(User user, Book book);

    List<UserBook> findByUserAndStatus(User user, ReadingStatus status);

    // kullanıcının tüm listesini için
    List<UserBook> findByUser(User user);
}