package com.kithub.controller;

import com.kithub.dto.CommentRequest;
import com.kithub.dto.CommentResponse;
import com.kithub.dto.UpdateCommentRequest;
import com.kithub.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/add")
    public ResponseEntity<CommentResponse> addComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CommentRequest request) {
        // Dönüş tipini CommentResponse yaptık
        return ResponseEntity.ok(commentService.addComment(userDetails.getUsername(), request));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateCommentRequest request) {

        return ResponseEntity.ok(commentService.updateComment(
                commentId,
                request.text(),
                request.starCount(),
                userDetails.getUsername()
        ));
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<CommentResponse>> getBookComments(@PathVariable String bookId) {
        // bookId artik long değil String (Google ID)
        return ResponseEntity.ok(commentService.getBookComments(bookId));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(commentService.deleteComment(commentId, userDetails.getUsername()));
    }
}