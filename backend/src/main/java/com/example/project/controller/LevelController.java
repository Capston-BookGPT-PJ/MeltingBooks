package com.example.project.controller;

import com.example.project.dto.request.AddExpRequest;
import com.example.project.enums.ExpEventType;
import com.example.project.repository.UserRepository;
import com.example.project.service.LevelService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/level")
@RequiredArgsConstructor
public class LevelController {

    private final LevelService levelService;
    private final UserRepository userRepository;

    @PostMapping("/give-exp")
    public String giveExp(@RequestParam Long userId, @RequestParam ExpEventType eventType) {
        return levelService.giveExp(userId, eventType);
    }

    @PostMapping("/add-exp")
    public String addExp(@RequestBody AddExpRequest request) {
        return levelService.addExperience(request.getUserId(), request.getPoints());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddExpRequest {
        private Long userId;
        private int points;
    }
}
