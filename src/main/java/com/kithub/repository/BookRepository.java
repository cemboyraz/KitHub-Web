package com.kithub.repository;

import com.kithub.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, String> { // ID tipini String yaptık


    default Optional<Book> findByGoogleBooksId(String googleBooksId) {
        return findById(googleBooksId);
    }

    List<Book> findByTitleContainingIgnoreCase(String title);

    List<Book> findByAuthorContainingIgnoreCase(String author);

    List<Book> findByCategory(String category);


    @Query(value = "SELECT * FROM books ORDER BY RANDOM() LIMIT 20", nativeQuery = true)
    List<Book> findRandomBooks();
}