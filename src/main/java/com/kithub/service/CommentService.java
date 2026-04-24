package com.kithub.service;

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

    @Transactional
    public String deleteComment(Long commentId, Long requestingUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Yorum bulunamadı!"));

        User requestingUser = userRepository.findById(requestingUserId)
                .orElseThrow(() -> new RuntimeException("İşlemi yapan kullanıcı bulunamadı!"));


        if (comment.getUser().getId().equals(requestingUser.getId()) || requestingUser.getRole() == Role.ADMIN) {
            commentRepository.delete(comment);
            return "Yorum başarıyla silindi.";
        } else {
            throw new RuntimeException("Güvenlik İhlali: Bu yorumu silmeye yetkiniz yok!");
        }
    }

    @Transactional
    public Comment updateComment(Long commentId, String newText, Integer newStarCount, Long requestingUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Yorum bulunamadı!"));

        if (!comment.getUser().getId().equals(requestingUserId)) {
            throw new RuntimeException("Sadece kendi yorumunuzu güncelleyebilirsiniz!");
        }

        comment.setText(newText);

        return commentRepository.save(comment);
    }
}