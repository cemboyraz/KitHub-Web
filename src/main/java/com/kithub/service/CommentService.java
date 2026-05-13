package com.kithub.service;

import com.kithub.dto.CommentRequest;
import com.kithub.dto.CommentResponse;
import com.kithub.model.Book;
import com.kithub.model.Comment;
import com.kithub.model.Role;
import com.kithub.model.User;
import com.kithub.repository.CommentRepository;
import com.kithub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final BookService bookService;
    private final UserService userService; // Banlamak için gerekli

    public List<CommentResponse> getBookComments(Long bookId) {
        Book book = bookService.getBookById(bookId);

        return commentRepository.findByBook(book)
                .stream()
                .map(comment -> new CommentResponse(
                        comment.getId(),
                        comment.getText(),
                        comment.getStarCount(),
                        comment.getUser().getUsername(), // İŞTE KRİTİK NOKTA: İsmi çekiyoruz
                        comment.getCreatedAt()
                ))
                .toList();
    }

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

        //  2 defa yorum yapamasın diye
        if (commentRepository.existsByUserAndBook(user, book)) {
            throw new RuntimeException("Bu kitaba zaten bir inceleme yazdınız! Fikriniz değiştiyse eski yorumunuzu güncelleyebilirsiniz.");
        }

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

        // Admin siliyorsa adamı otomatik banla
        if (requestingUser.getRole() == Role.ADMIN) {
            commentRepository.delete(comment);
            userService.banUser(author.getId());
            return "Yorum admin tarafından silindi. Kural ihlali yapan kullanıcı (" + author.getUsername() + ") otomatik olarak banlandı!";
        }
        //  Kullanıcı kendi yorumunu siliyorsa
        else if (author.getId().equals(requestingUser.getId())) {
            commentRepository.delete(comment);
            return "Yorumunuz başarıyla silindi.";
        }
        //  Başkasının yorumunu silmeye çalışıyorsa aslında direkt olarak buton da koymayabilirim fronta bu text kalsın kafamda belirsizlik var
        else {
            throw new RuntimeException("Güvenlik İhlali: Bu yorumu silmeye yetkiniz yok!");
        }
    }

    @Transactional
    public CommentResponse updateComment(Long commentId, String newText, Integer newStarCount, String requesterEmail) {
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

        Comment updatedComment = commentRepository.save(comment);

        return new CommentResponse(
                updatedComment.getId(),
                updatedComment.getText(),
                updatedComment.getStarCount(),
                updatedComment.getUser().getUsername(),
                updatedComment.getCreatedAt()
        );
    }
}