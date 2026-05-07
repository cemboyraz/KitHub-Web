package com.kithub.controller;

import com.kithub.dto.CommentRequest;
import com.kithub.model.Comment;
import com.kithub.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;


    @PostMapping("/add")
    public ResponseEntity<Comment> addComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CommentRequest request) {

        return ResponseEntity.ok(commentService.addComment(userDetails.getUsername(), request));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<Comment> updateComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String text,
            @RequestParam(required = false) Integer starCount) {

        return ResponseEntity.ok(commentService.updateComment(commentId, text, starCount, userDetails.getUsername()));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(commentService.deleteComment(commentId, userDetails.getUsername()));
    }
}