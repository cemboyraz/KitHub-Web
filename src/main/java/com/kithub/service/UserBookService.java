package com.kithub.service;

import com.kithub.model.Book;
import com.kithub.model.ReadingStatus;
import com.kithub.model.User;
import com.kithub.model.UserBook;
import com.kithub.repository.UserBookRepository;
import com.kithub.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserBookService {

    private final UserBookRepository userBookRepository;
    private final UserRepository userRepository;
    private final BookService bookService;

    public UserBookService(UserBookRepository userBookRepository, UserRepository userRepository, BookService bookService) {
        this.userBookRepository = userBookRepository;
        this.userRepository = userRepository;
        this.bookService = bookService;
    }


    @Transactional
    public UserBook addOrUpdateLibrary(Long userId, String googleId, String title, String author, String summary, String imageUrl, String category, ReadingStatus status) { // <-- Category String oldu!

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));


        Book savedBook = bookService.saveBookIfNotExists(googleId, title, author, summary, imageUrl, category);

        return userBookRepository.findByUserAndBook(user,savedBook)
                .map(existingRecord -> {
                    existingRecord.setStatus(status);
                    return userBookRepository.save(existingRecord);
                })
                .orElseGet(() -> {
                    UserBook newUserBook = new UserBook();
                    newUserBook.setUser(user);
                    newUserBook.setBook(savedBook);
                    newUserBook.setStatus(status);
                    return userBookRepository.save(newUserBook);
                });
    }

    public List<String> getUserFinishedBooksForAI(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));

        return userBookRepository.findByUserAndStatus(user, ReadingStatus.FINISHED)
                .stream()
                .map(userBook -> userBook.getBook().getTitle())
                .toList();
    }
}