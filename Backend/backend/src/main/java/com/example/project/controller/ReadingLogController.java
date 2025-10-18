package com.example.project.controller;

import com.example.project.common.ApiResponse;
import com.example.project.dto.ReadingLogDto;
import com.example.project.dto.ReadingLogStatsDto;
import com.example.project.service.ReadingLogQueryService;
import com.example.project.service.ReadingLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/logs")
@RequiredArgsConstructor
public class ReadingLogController {

    private final ReadingLogService logService;
    private final ReadingLogQueryService readingLogQueryService;

    // 생성 (bookId 필요)
    @PostMapping("/books/{bookId}")
    public ResponseEntity<ApiResponse<ReadingLogDto>> createLog(
            @PathVariable Long userId,
            @PathVariable Long bookId,
            @RequestBody ReadingLogDto dto
    ) {
        return ResponseEntity.ok(ApiResponse.success(logService.create(userId, bookId, dto)));
    }

    // 전체 조회 (기간 파라미터 없는 경우)
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReadingLogDto>>> getLogs(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(logService.getByUser(userId)));
    }

    // 기간 조회 (from, to 파라미터가 모두 있을 때만 매핑)
    @GetMapping(params = {"from", "to"})
    public ResponseEntity<ApiResponse<List<ReadingLogDto>>> getLogsInRange(
            @PathVariable Long userId,
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        try {
            List<ReadingLogDto> data = readingLogQueryService.getLogs(userId, from, to);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.error("INVALID_ARGUMENT", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("INTERNAL_ERROR", "알 수 없는 오류가 발생했습니다."));
        }
    }

    // 수정
    @PutMapping("/{logId}")
    public ResponseEntity<ApiResponse<ReadingLogDto>> updateLog(
            @PathVariable Long userId,
            @PathVariable Long logId,
            @RequestBody ReadingLogDto dto
    ) {
        return ResponseEntity.ok(ApiResponse.success(logService.update(logId, dto)));
    }

    // 삭제 (userId와 logId 같이 전달)
    @DeleteMapping("/{logId}")
    public ResponseEntity<Void> deleteLog(
            @PathVariable Long userId,
            @PathVariable Long logId
    ) {
        logService.delete(userId, logId);
        return ResponseEntity.noContent().build(); // 204
    }

    // 통계
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<ReadingLogStatsDto>> getStats(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(logService.getStats(userId)));
    }
}
