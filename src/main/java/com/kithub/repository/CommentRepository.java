package com.kithub.repository;

import com.kithub.model.Book;
import com.kithub.model.Comment;
import com.kithub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Bir kitabın detay sayfasına girildiğinde o kitabın altındaki tüm yorumları listele
    List<Comment> findByBook(Book book);

    // Kullanıcı bir kitaba daha önce yorum yapmış mı diye kontrol et bir kitaba 2 defa puan olamz
    boolean existsByUserAndBook(User user, Book book);
}