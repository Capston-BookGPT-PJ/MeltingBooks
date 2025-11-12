package com.example.project.controller;

import com.example.project.common.ApiResponse;
import com.example.project.dto.CommentDto;
import com.example.project.dto.request.CommentUpdateRequest;
import com.example.project.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /** 댓글 생성 */
    @PostMapping
    public ResponseEntity<ApiResponse<CommentDto>> create(
            @RequestAttribute Long userId,      // ������ 토큰에서 주입
            @RequestParam Long reviewId,
            @RequestBody CommentUpdateRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                commentService.create(userId, reviewId, req.getContent())
        ));
    }

    /** 댓글 수정 */
    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentDto>> update(
            @PathVariable Long commentId,
            @RequestAttribute Long userId,      // ������ 토큰에서 주입
            @RequestBody CommentUpdateRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                commentService.update(commentId, userId, req.getContent())
        ));
    }

    /** 댓글 삭제 */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long commentId,
            @RequestAttribute Long userId       // ������ 토큰에서 주입
    ) {
        commentService.delete(commentId, userId);
        return ResponseEntity.noContent().build();
    }

    /** 특정 리뷰의 댓글 조회 (likedByMe 계산을 위해 현재 사용자 선택적 전달) */
    @GetMapping("/review/{reviewId}")
    public ResponseEntity<ApiResponse<List<CommentDto>>> getByReview(
            @PathVariable Long reviewId,
            @RequestAttribute(required = false) Long userId // ������ 비로그인 허용
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                commentService.getByReview(reviewId, userId)
        ));
    }
}
