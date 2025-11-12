package com.example.project.controller;

import com.example.project.common.ApiResponse;
import com.example.project.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ImageController {
    private final ImageService imageService;

    @PostMapping(
        value = "/users/{userId}/profile-image",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<String>> uploadProfileImage(
        @PathVariable Long userId,
        @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(ApiResponse.success(imageService.uploadProfileImage(userId, file)));
    }

    @PostMapping(
        value = "/reviews/{reviewId}/review-images",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<List<String>>> uploadReviewImages(
        @PathVariable Long reviewId,
        @RequestParam("files") List<MultipartFile> files
    ) {
        return ResponseEntity.ok(ApiResponse.success(imageService.uploadReviewImages(reviewId, files)));
    }
}

