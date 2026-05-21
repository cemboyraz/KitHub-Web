package com.kithub.service;

import com.kithub.dto.UserBookResponse;
import com.kithub.model.Book;
import com.kithub.model.ReadingStatus;
import com.kithub.model.User;
import com.kithub.model.UserBook;
import com.kithub.repository.BookRepository;
import com.kithub.repository.CommentRepository;
import com.kithub.repository.UserBookRepository;
import com.kithub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserBookService {

    private final UserBookRepository userBookRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final CommentRepository commentRepository;


    @Transactional
    public UserBook addOrUpdateLibrary(String email, String googleId, String title, String author, String summary, String imageUrl, String category, ReadingStatus status) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));

        // Kitabı bul veya yarat
        Book book = bookRepository.findById(googleId)
                .orElseGet(() -> {
                    Book newBook = new Book();
                    newBook.setId(googleId);
                    newBook.setTitle(title != null ? title : "Unknown Title");
                    newBook.setAuthor(author != null ? author : "Unknown Author");
                    newBook.setSummary(summary != null && summary.length() > 1900 ? summary.substring(0, 1900) + "..." : (summary != null ? summary : "No description"));
                    newBook.setImageUrl(imageUrl != null ? imageUrl : "");
                    newBook.setCategory(category != null ? category : "Unknown");
                    return bookRepository.save(newBook);
                });

        UserBook userBook = userBookRepository.findByUserAndBook_Id(user, googleId)
                .orElse(new UserBook()); // Yoksa yenisini al

        // Eğer yeni bir kayıt oluşturuyorsak ilişkileri kur
        if (userBook.getId() == null) {
            userBook.setUser(user);
            userBook.setBook(book);
        }

        // Durumu güncelle
        userBook.setStatus(status);
        return userBookRepository.save(userBook);
    }

    public List<String> getUserFinishedBooksForAI(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));

        return userBookRepository.findByUserAndStatus(user, ReadingStatus.FINISHED)
                .stream()
                .map(userBook -> userBook.getBook().getTitle())
                .toList();
    }


    // kullanıcnın kitap listesi yani kütüphanesi
    public List<UserBookResponse> getUserLibrary(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));

        return userBookRepository.findByUser(user)
                .stream()
                .map(userbook -> new UserBookResponse(
                        userbook.getId(),
                        userbook.getBook().getId(),
                        userbook.getBook().getTitle(),
                        userbook.getBook().getAuthor(),
                        userbook.getBook().getImageUrl(),
                        userbook.getBook().getSummary(),
                        userbook.getStatus(),
                        userbook.getAddedAt()
                ))
                .toList();
    }

    @Transactional
    public void removeFromLibrary(String email, String googleBooksId) {
        User user = userRepository.findByEmail(email).orElseThrow();

        //  SADECE bu kullanıcının o kitaptaki yorumunu siliyoruz Başkalarınınki güvende
        commentRepository.deleteByUserAndBook_Id(user, googleBooksId);

        // KİTABI LİSTEDEN ÇIKAR
        List<UserBook> userBooks = userBookRepository.findAllByUserAndBook_Id(user, googleBooksId);
        if (!userBooks.isEmpty()) {
            userBookRepository.deleteAll(userBooks);
        }
    }
    }

