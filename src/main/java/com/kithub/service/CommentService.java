package com.kithub.service;

import com.kithub.dto.CommentRequest;
import com.kithub.model.Book;
import com.kithub.model.Comment;
import com.kithub.model.Role;
import com.kithub.model.User;
import com.kithub.repository.CommentRepository;
import com.kithub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final BookService bookService;
    private final UserService userService; // Banlamak için gerekli

    @Transactional
    public Comment addComment(String email, CommentRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        // Kitap yoksa Google verisiyle önce yarat, varsa getir
        Book book = bookService.saveBookIfNotExists(
                request.googleBooksId(),
                request.title(),
                request.author(),
                request.summary(),
                request.imageUrl(),
                request.category()
        );

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setBook(book);
        comment.setText(request.text());
        comment.setStarCount(request.starCount());

        return commentRepository.save(comment);
    }

    @Transactional
    public String deleteComment(Long commentId, String requesterEmail) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Yorum bulunamadı!"));

        User requestingUser = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new RuntimeException("İşlemi yapan kullanıcı bulunamadı!"));

        User author = comment.getUser();

        // 1. Durum: Admin siliyorsa adamı otomatik banla[cite: 1]
        if (requestingUser.getRole() == Role.ADMIN) {
            commentRepository.delete(comment);
            userService.banUser(author.getId());
            return "Yorum admin tarafından silindi. Kural ihlali yapan kullanıcı (" + author.getUsername() + ") otomatik olarak banlandı!";
        }
        // 2. Durum: Kullanıcı kendi yorumunu siliyorsa
        else if (author.getId().equals(requestingUser.getId())) {
            commentRepository.delete(comment);
            return "Yorumunuz başarıyla silindi.";
        }
        // 3. Durum: Başkasının yorumunu silmeye çalışıyorsa
        else {
            throw new RuntimeException("Güvenlik İhlali: Bu yorumu silmeye yetkiniz yok!");
        }
    }

    @Transactional
    public Comment updateComment(Long commentId, String newText, Integer newStarCount, String requesterEmail) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Yorum bulunamadı!"));

        User requestingUser = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new RuntimeException("İşlemi yapan kullanıcı bulunamadı!"));

        if (!comment.getUser().getId().equals(requestingUser.getId())) {
            throw new RuntimeException("Sadece kendi yorumunuzu güncelleyebilirsiniz!");
        }

        comment.setText(newText);
        if (newStarCount != null) {
            comment.setStarCount(newStarCount);
        }

        return commentRepository.save(comment);
    }
}