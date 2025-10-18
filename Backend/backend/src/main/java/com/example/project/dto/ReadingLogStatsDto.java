package com.example.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadingLogStatsDto {
    private Long userId;
    private int totalPages;
    private int totalMinutes;
    private int completedBooks;
    private LocalDate firstLogDate;
    private LocalDate lastLogDate;
}
