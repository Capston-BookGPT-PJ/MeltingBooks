package com.example.project.controller;

import com.example.project.common.ApiResponse;
import com.example.project.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> likeReview(
            @PathVariable Long reviewId,
            @RequestAttribute Long userId
    ) {
        likeService.likeReview(reviewId, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> unlikeReview(
            @PathVariable Long reviewId,
            @RequestAttribute Long userId
    ) {
        likeService.unlikeReview(reviewId, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> likeComment(
            @PathVariable Long commentId,
            @RequestAttribute Long userId
    ) {
        likeService.likeComment(commentId, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> unlikeComment(
            @PathVariable Long commentId,
            @RequestAttribute Long userId
    ) {
        likeService.unlikeComment(commentId, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
