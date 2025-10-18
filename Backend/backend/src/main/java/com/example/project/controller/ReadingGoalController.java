package com.example.project.controller;

import com.example.project.common.ApiResponse;
import com.example.project.dto.ReadingGoalDto;
import com.example.project.entity.ReadingGoal.GoalType;
import com.example.project.service.ReadingGoalService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class ReadingGoalController {

    private final ReadingGoalService goalService;

    /** 목표 생성 (헤더 토큰에서 userId 주입) */
    @PostMapping
    public ResponseEntity<ReadingGoalDto> createGoal(@RequestAttribute Long userId,
                                                     @RequestBody ReadingGoalDto dto) {
        return ResponseEntity.ok(goalService.createGoal(userId, dto));
    }

    /** 내 목표 목록 */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<ReadingGoalDto>>> myGoals(@RequestAttribute Long userId) {
        List<ReadingGoalDto> goals = goalService.getGoalsByUserWithBooks(userId);
        return ResponseEntity.ok(ApiResponse.success(goals));
    }

    /** 내 목표 수정 (기간 기반 유지 시 사용) */
    @PutMapping("/me")
    public ResponseEntity<ReadingGoalDto> updateMyGoal(@RequestAttribute Long userId,
                                                       @RequestBody ReadingGoalDto dto) {
        return ResponseEntity.ok(goalService.updateGoalByPeriod(userId, dto));
    }
    
    @PutMapping("/{goalId}")
public ResponseEntity<ReadingGoalDto> updateGoalById(
        @RequestAttribute Long userId,
        @PathVariable Long goalId,
        @RequestBody ReadingGoalDto dto
) {
    return ResponseEntity.ok(goalService.updateGoalById(userId, goalId, dto));
}

    /** ✅ 내 목표 삭제 (goalId 기준) */
    @DeleteMapping("/{goalId}")
    public ResponseEntity<Void> deleteMyGoalById(@RequestAttribute Long userId,
                                                 @PathVariable Long goalId) {
        goalService.deleteMyGoalById(userId, goalId);
        return ResponseEntity.noContent().build();
    }

    /** 현재 진행중인 내 목표들에 책 1권 완료 반영 */
    @PostMapping("/me/complete-book")
    public ResponseEntity<Void> completeBookForMe(@RequestAttribute Long userId) {
        goalService.completeBookByUser(userId);
        return ResponseEntity.noContent().build();
    }

    /** 현재 진행중인 내 목표들에 감상문 1개 완료 반영 */
    @PostMapping("/me/complete-review")
    public ResponseEntity<Void> completeReviewForMe(@RequestAttribute Long userId) {
        goalService.completeReviewByUser(userId);
        return ResponseEntity.noContent().build();
    }
    
    /** ✅ 완독 목록/수 재계산(백필용) */
    @PostMapping("/{goalId}/recompute-books")
    public ResponseEntity<ApiResponse<ReadingGoalDto>> recomputeBooks(@PathVariable Long goalId) {
        return ResponseEntity.ok(ApiResponse.success(goalService.recomputeBooks(goalId)));
    }

    @Data
    public static class AddTimeRequest {
        private int minutes;
    }
}
