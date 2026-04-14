package com.kithub.repository;

import com.kithub.model.Book;
import com.kithub.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    //Kullanıcı kitap eklerken "Bu kitap zaten bizde var mı?" diye Google ID ile bakar.
    Optional<Book> findByGoogleBooksId(String googleBooksId);

    //Admin panelinde kitap adına göre arama yapmak için .
    List<Book> findByTitleContainingIgnoreCase(String title);

    //Admin panelinde yazara göre arama yapmak için.
    List<Book> findByAuthorContainingIgnoreCase(String author);

    //Kategoriye göre filtrelemek için.
    List<Book> findByCategory(Category category);
}