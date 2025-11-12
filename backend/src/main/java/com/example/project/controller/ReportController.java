package com.example.project.controller;

import com.example.project.common.ApiResponse;
import com.example.project.dto.ReportDto;
import com.example.project.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // 월간 독서 리포트 조회
    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<ReportDto>> getMonthlyReport(
            @RequestAttribute Long userId,
            @RequestParam int year,
            @RequestParam int month) {
        
        ReportDto report = reportService.getMonthlyReport(userId, year, month);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    // 연간 독서 리포트 조회 엔드포인트
    @GetMapping("/yearly")
    public ResponseEntity<ApiResponse<ReportDto>> getYearlyReport(
            @RequestAttribute Long userId,
            @RequestParam int year) {
        
        ReportDto report = reportService.getYearlyReport(userId, year);
        return ResponseEntity.ok(ApiResponse.success(report));
    }
}
