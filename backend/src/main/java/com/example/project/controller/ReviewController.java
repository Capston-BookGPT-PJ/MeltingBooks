package com.example.project.controller;

import com.example.project.common.ApiResponse;
import com.example.project.dto.ReviewDto;
import com.example.project.dto.PopularHashtagDto;
import com.example.project.dto.request.CreateReviewRequest;
import com.example.project.dto.request.UpdateReviewRequest;
import com.example.project.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 생성(JSON)
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewDto>> create(
            @RequestBody CreateReviewRequest req,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.create(
                        userId,
                        req.getBookId(),
                        req.getContent(),
                        req.getRating(),
                        null,
                        req.getHashtags(),
                        req.getImageUrl() != null ? List.of(req.getImageUrl()) : req.getImageUrls()
                )
        ));
    }

    // 생성(멀티파트)
    @PostMapping(value = "/multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ReviewDto>> createMultipart(
            @RequestPart(value = "bookId", required = false) Long bookId,
            @RequestPart("content") String content,
            @RequestPart(value = "rating", required = false) Integer rating,
            @RequestPart(value = "hashtags", required = false) List<String> hashtags,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestPart(value = "imageUrls", required = false) List<String> imageUrls,
            @RequestAttribute Long userId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.create(userId, bookId, content, rating, files, hashtags, imageUrls)
        ));
    }

    // 수정(JSON)
    @PutMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewDto>> update(
            @PathVariable Long reviewId,
            @RequestAttribute Long userId,
            @RequestBody UpdateReviewRequest req) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.update(
                        reviewId,
                        userId,
                        req.getContent(),
                        req.getRating(),
                        req.getHashtags(),
                        req.getImageUrls(),
                        null,
                        Boolean.FALSE,
                        req.getBookId()
                )
        ));
    }

    // 수정(멀티파트)
    @PutMapping(value = "/{reviewId}/multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ReviewDto>> updateMultipart(
            @PathVariable Long reviewId,
            @RequestAttribute Long userId,
            @RequestPart(value = "content", required = false) String content,
            @RequestPart(value = "rating", required = false) Integer rating,
            @RequestPart(value = "hashtags", required = false) List<String> hashtags,
            @RequestPart(value = "imageUrls", required = false) List<String> imageUrls,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestPart(value = "clearImages", required = false) Boolean clearImages,
            @RequestPart(value = "bookId", required = false) Long bookId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.update(
                        reviewId, userId, content, rating, hashtags, imageUrls, files,
                        clearImages != null ? clearImages : false,
                        bookId
                )
        ));
    }

    // ✅ 단건 상세 (like 정보 포함)
    @GetMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewDto>> get(
            @PathVariable Long reviewId,
            @RequestAttribute(name = "userId", required = false) Long viewerId) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.get(reviewId, viewerId)
        ));
    }

    // ✅ 전체(피드) (like 정보 포함)
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReviewDto>>> getAll(
            @RequestAttribute(name = "userId", required = false) Long viewerId) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.getAll(viewerId)
        ));
    }

    // ✅ 특정 유저 리뷰 (like 정보 포함)
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<ReviewDto>>> getByUser(
            @PathVariable Long userId,
            @RequestAttribute(name = "userId", required = false) Long viewerId) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.getByUser(userId, viewerId)
        ));
    }

    // ✅ 특정 책 리뷰 (like 정보 포함)
    @GetMapping("/book/{bookId}")
    public ResponseEntity<ApiResponse<List<ReviewDto>>> getByBook(
            @PathVariable Long bookId,
            @RequestAttribute(name = "userId", required = false) Long viewerId) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.getByBook(bookId, viewerId)
        ));
    }

    // 삭제
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long reviewId,
            @RequestAttribute Long userId) {
        reviewService.delete(reviewId, userId);
        return ResponseEntity.noContent().build();
    }

    // 인기 해시태그
    @GetMapping("/popular-tags")
    public ResponseEntity<ApiResponse<List<PopularHashtagDto>>> popularTags() {
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.getPopularHashtagsTop20()
        ));
    }

    // ✅ 해시태그 검색 (like 정보 포함)
    @GetMapping("/search-by-hashtag")
    public ResponseEntity<ApiResponse<Page<ReviewDto>>> searchByHashtag(
            @RequestParam String hashtag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestAttribute(name = "userId", required = false) Long viewerId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.searchReviewsByHashtag(hashtag, page, size, viewerId)
        ));
    }
}
