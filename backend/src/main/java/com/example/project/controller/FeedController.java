package com.example.project.controller;

import com.example.project.common.ApiResponse;
import com.example.project.dto.FeedDto;
import com.example.project.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<FeedDto>>> getFeed(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<FeedDto> feedPage = feedService.getFeed(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(feedPage));
    }
}
