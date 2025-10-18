package com.example.project.controller;

import com.example.project.common.ApiResponse;
import com.example.project.dto.BadgeDto;
import com.example.project.dto.BadgeListItemDto;
import com.example.project.enums.BadgeType;
import com.example.project.service.badge.BadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/badges")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;

    /** 단일 타입 평가 & 필요시 지급 */
    @PostMapping("/evaluate")
    public ResponseEntity<ApiResponse<List<BadgeDto>>> evaluateOne(
            @RequestAttribute Long userId,
            @RequestParam BadgeType type
    ) {
        var updated = badgeService.evaluateAndAward(type, userId)
                .map(b -> List.of(BadgeDto.from(b)))
                .orElse(List.of());
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /** 전체 타입 평가 & 필요시 지급 */
    @PostMapping("/evaluate-all")
    public ResponseEntity<ApiResponse<List<BadgeDto>>> evaluateAll(
            @RequestAttribute Long userId
    ) {
        var result = badgeService.evaluateAll(userId)
                .stream().map(BadgeDto::from).toList();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /** 내가 가진 배지 목록 */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BadgeDto>>> getBadges(
            @RequestAttribute Long userId
    ) {
        var result = badgeService.getUserBadges(userId)
                .stream().map(BadgeDto::from).toList();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /** 전체 배지(미획득 포함) 카탈로그: 내 획득/진행상태 포함 */
    @GetMapping("/catalog")
    public ResponseEntity<ApiResponse<List<BadgeListItemDto>>> getCatalog(
            @RequestAttribute Long userId
    ) {
        var result = badgeService.getBadgeCatalog(userId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}

