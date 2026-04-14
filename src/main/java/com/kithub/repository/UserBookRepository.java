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

    // kullanıcı bir kitabı eklerken bu kitap zaten listemde var mı diye kontrol etcek.
    Optional<UserBook> findByUserAndBook(User user, Book book);

    // yapay zekaya okunan kitapları göndermek için adamın FINISHED(bitirilmiş) olan kitaplarını getircek ki ona göre değerlendirsin.
    List<UserBook> findByUserAndStatus(User user, ReadingStatus status);

    // kullanıcının tüm listesini getirmesi için
    List<UserBook> findByUser(User user);
}