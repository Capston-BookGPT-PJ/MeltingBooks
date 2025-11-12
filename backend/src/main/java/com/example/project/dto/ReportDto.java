package com.example.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportDto {
    private Integer totalPages;
    private Integer totalMinutes;
    private Long completedReviews;

    private Integer averageDailyMinutes;
    private List<BadgeDto> badges; // 이번 달/올해 획득 배지
    private Integer experience;
    private Integer level;
}
