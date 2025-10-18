package com.example.project.controller;

import com.example.project.common.ApiResponse;
import com.example.project.dto.BookDto;
import com.example.project.dto.ReviewDto;
import com.example.project.dto.UserDto;
import com.example.project.entity.User;
import com.example.project.repository.ReadingLogRepository;
import com.example.project.repository.ReviewRepository;
import com.example.project.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;
    private final ReadingLogRepository readingLogRepository;
    private final ReviewRepository reviewRepository;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDto>> get(@PathVariable Long userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 최근 읽은 책 (최신 독서 기록 3개) — id 기반 메서드로 변경
        var logs = readingLogRepository
                .findByUser_IdOrderByReadAtDesc(u.getId(), PageRequest.of(0, 3));
        List<BookDto> recentBooks = logs.stream()
                .map(l -> BookDto.from(l.getBook()))
                .collect(Collectors.toList());

        // 최근 작성한 글 (최신 리뷰 3개)
        List<ReviewDto> recentReviews = reviewRepository
                .findByUserOrderByCreatedAtDesc(u, PageRequest.of(0, 3)).stream()
                .map(ReviewDto::from)
                .collect(Collectors.toList());

        UserDto userDto = UserDto.from(u);
        userDto.setRecentBooks(recentBooks);
        userDto.setRecentReviews(recentReviews);

        return ResponseEntity.ok(ApiResponse.success(userDto));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> update(@PathVariable Long userId,
                                                    @RequestBody UpdateProfileRequest req) {
        User u = userRepository.findById(userId).orElseThrow();
        safeSet(u, "nickname", req.getNickname());
        safeSet(u, "tagId", req.getTagId());
        safeSet(u, "bio", req.getBio());
        safeSet(u, "profileImageUrl", req.getProfileImageUrl());
        userRepository.save(u);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void safeSet(User u, String field, Object value) {
        if (value == null) return;
        Field f = ReflectionUtils.findField(User.class, field);
        if (f == null) return;
        f.setAccessible(true);
        try { f.set(u, value); } catch (IllegalAccessException ignored) {}
    }

    @Data
    public static class UpdateProfileRequest {
        private String nickname;
        private String tagId;
        private String bio;
        private String profileImageUrl;
    }
}
