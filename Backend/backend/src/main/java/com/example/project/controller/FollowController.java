package com.example.project.controller;

import com.example.project.common.ApiResponse;
import com.example.project.dto.FollowDto;
import com.example.project.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    // 팔로우
    @PostMapping
public ResponseEntity<ApiResponse<String>> follow(@RequestParam Long followerId,
                                                  @RequestParam Long followingId) {
    followService.follow(followerId, followingId);
    return ResponseEntity.ok(ApiResponse.success("Followed successfully"));
}

    // 언팔로우
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> unfollow(@RequestParam Long followerId,
                                                      @RequestParam Long followingId) {
        followService.unfollow(followerId, followingId);
        return ResponseEntity.noContent().build();
    }

    // 팔로워 조회
    @GetMapping("/followers/{userId}")
    public ResponseEntity<ApiResponse<List<FollowDto>>> followers(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(followService.followers(userId)));
    }

    // 팔로잉 조회
    @GetMapping("/following/{userId}")
    public ResponseEntity<ApiResponse<List<FollowDto>>> following(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(followService.following(userId)));
    }
}

