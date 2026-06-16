package com.kithub.service;

import com.kithub.dto.CommentRequest;
import com.kithub.dto.CommentResponse;
import com.kithub.model.Book;
import com.kithub.model.Comment;
import com.kithub.model.Role;
import com.kithub.model.User;
import com.kithub.repository.BookRepository;
import com.kithub.repository.CommentRepository;
import com.kithub.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final BookService bookService;
    private final UserService userService;
    private final BookRepository bookRepository;

    public List<CommentResponse> getBookComments(String bookId) {
        try {
            Book book = bookService.getBookById(bookId);

            return commentRepository.findByBook(book)
                    .stream()
                    .map(comment -> new CommentResponse(
                            comment.getId(),
                            comment.getText(),
                            comment.getStarCount(),
                            comment.getUser().getUsername(),
                            comment.getCreatedAt()
                    ))
                    .toList();
        } catch (RuntimeException e) {
            return java.util.List.of();
        }
    }

    @Transactional
    public CommentResponse addComment(String email, CommentRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        Book book = bookService.saveBookIfNotExists(
                request.googleBooksId(),
                request.title(),
                request.author(),
                request.summary(),
                request.imageUrl(),
                request.category()
        );

        if (commentRepository.existsByUserAndBook(user, book)) {
            throw new RuntimeException("Bu kitaba zaten bir inceleme yazdınız!");
        }

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setBook(book);
        comment.setText(request.text());
        comment.setStarCount(request.starCount());

        Comment savedComment = commentRepository.save(comment);

        //  Yeni yorum atıldığında kitabın genel puanını GÜNCELLE!
        updateBookRating(book);

        return new CommentResponse(
                savedComment.getId(),
                savedComment.getText(),
                savedComment.getStarCount(),
                savedComment.getUser().getUsername(),
                savedComment.getCreatedAt()
        );
    }

    @Transactional
    public String deleteComment(Long commentId, String requesterEmail) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Yorum bulunamadı!"));

        User requestingUser = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new RuntimeException("İşlemi yapan kullanıcı bulunamadı!"));

        User author = comment.getUser();
        Book book = comment.getBook(); // Kitabı silinmeden önce hafızaya alıyoruz

        if (requestingUser.getRole() == Role.ADMIN) {
            commentRepository.delete(comment);
            userService.banUser(author.getId());
            updateBookRating(book); // Banlanınca ortalamayı güncelle
            return "Kural ihlali! Yorum silindi ve kullanıcı banlandı.";
        }

        if (author.getId().equals(requestingUser.getId())) {
            commentRepository.delete(comment);

            //  Yorum silindiğinde kitabın genel puanını GÜNCELLE!
            updateBookRating(book);

            return "Yorumunuz silindi.";
        }

        throw new RuntimeException("Yetkisiz işlem!");
    }

    @Transactional
    public CommentResponse updateComment(Long commentId, String newText, Integer newStarCount, String email) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Yorum bulunamadı"));

        User user = userRepository.findByEmail(email).get();

        if(!comment.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Sadece kendi yorumunu güncelleyebilirsin!");
        }

        comment.setText(newText);
        if(newStarCount != null) {
            comment.setStarCount(newStarCount);
        }

        Comment updated = commentRepository.save(comment);

        // güncellenmesi için .
        updateBookRating(updated.getBook());

        return new CommentResponse(
                updated.getId(),
                updated.getText(),
                updated.getStarCount(),
                updated.getUser().getUsername(),
                updated.getCreatedAt()
        );
    }

    private void updateBookRating(Book book) {
        List<Comment> allComments = commentRepository.findByBook(book);
        double avgRating = allComments.stream()
                .mapToInt(Comment::getStarCount)
                .average()
                .orElse(0.0);

        book.setAverageRating((float) avgRating);
        book.setTotalReviews(allComments.size());
        bookRepository.save(book);
    }
}