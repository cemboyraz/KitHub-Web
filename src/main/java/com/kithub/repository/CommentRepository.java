package com.kithub.repository;

import com.kithub.model.Book;
import com.kithub.model.Comment;
import com.kithub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {


    List<Comment> findByBook(Book book);

    boolean existsByUserAndBook(User user, Book book);
}