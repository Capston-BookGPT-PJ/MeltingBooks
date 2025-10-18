package com.example.project.controller;

import com.example.project.common.ApiResponse;
import com.example.project.dto.GroupGoalDto;
import com.example.project.service.GroupGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/goals")
@RequiredArgsConstructor
public class GroupGoalController {

    private final GroupGoalService groupGoalService;

    @PostMapping
    public ResponseEntity<ApiResponse<GroupGoalDto>> createGoal(
            @PathVariable Long groupId,
            @RequestAttribute Long userId,
            @RequestBody GroupGoalDto req
    ) {
        return ResponseEntity.ok(ApiResponse.success(groupGoalService.create(groupId, userId, req)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GroupGoalDto>>> list(@PathVariable Long groupId) {
        return ResponseEntity.ok(ApiResponse.success(groupGoalService.findByGroup(groupId)));
    }

    @GetMapping("/{goalId}")
    public ResponseEntity<ApiResponse<GroupGoalDto>> get(
            @PathVariable Long groupId,   // 바인딩만 (서비스에서 검증 추가 예정)
            @PathVariable Long goalId
    ) {
        return ResponseEntity.ok(ApiResponse.success(groupGoalService.get(goalId)));
    }

    @PutMapping("/{goalId}")
    public ResponseEntity<ApiResponse<GroupGoalDto>> update(
            @PathVariable Long groupId,   // 바인딩만
            @PathVariable Long goalId,
            @RequestAttribute Long userId,
            @RequestBody GroupGoalDto req
    ) {
        return ResponseEntity.ok(ApiResponse.success(groupGoalService.update(goalId, userId, req)));
    }

    @DeleteMapping("/{goalId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long groupId,   // 바인딩만
            @PathVariable Long goalId,
            @RequestAttribute Long userId
    ) {
        groupGoalService.delete(goalId, userId);
        return ResponseEntity.noContent().build();
    }

    // 진행 재계산(관리/스케줄러 점검용)
    @PostMapping("/{goalId}/recompute")
    public ResponseEntity<ApiResponse<GroupGoalDto>> recompute(
            @PathVariable Long groupId,   // 바인딩만
            @PathVariable Long goalId
    ) {
        return ResponseEntity.ok(ApiResponse.success(groupGoalService.recompute(goalId)));
    }
}
