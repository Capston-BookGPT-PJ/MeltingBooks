package com.example.project.service;

import com.example.project.dto.ReportDto;
import com.example.project.dto.BadgeDto;
import com.example.project.entity.User;
import com.example.project.repository.ReadingLogRepository;
import com.example.project.repository.ReadingGoalRepository;
import com.example.project.repository.ReviewRepository;
import com.example.project.repository.UserRepository;
import com.example.project.repository.BadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReadingLogRepository readingLogRepository;
    private final ReadingGoalRepository readingGoalRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final BadgeRepository badgeRepository;

    @Transactional(readOnly = true)
    public ReportDto getMonthlyReport(Long userId, int year, int month) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // [start, end) 상한 제외 규약
        LocalDateTime start = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime end   = YearMonth.of(year, month).plusMonths(1).atDay(1).atStartOfDay();

        long totalPagesL   = readingLogRepository.sumPagesByUserIdAndPeriod(user.getId(), start, end);
        long totalMinutesL = readingLogRepository.sumMinutesByUserIdAndPeriod(user.getId(), start, end);

        // ✅ 리뷰 수: [start, end) 규약의 새 메서드 사용
        long completedReviews = reviewRepository.countByUserIdAndCreatedAtRange(user.getId(), start, end);

        int daysInMonth = YearMonth.of(year, month).lengthOfMonth();
        int averageDailyMinutes = (daysInMonth > 0)
                ? Math.toIntExact(Math.round((double) totalMinutesL / daysInMonth))
                : 0;

        // 배지는 기존 between(포함) 쿼리를 쓰고 있다면 end-1ns 유지, 아니면 레포도 [start, end)로 맞춰주는 걸 권장
        List<BadgeDto> badges = badgeRepository
                .findByUserIdAndCreatedAtBetween(user.getId(), start, end.minusNanos(1))
                .stream()
                .map(BadgeDto::from)
                .collect(Collectors.toList());

        int experience = user.getExperience();
        int level      = user.getLevel();

        return ReportDto.builder()
                .totalPages(Math.toIntExact(totalPagesL))
                .totalMinutes(Math.toIntExact(totalMinutesL))
                .completedReviews(completedReviews)
                .averageDailyMinutes(averageDailyMinutes)
                .badges(badges)
                .experience(experience)
                .level(level)
                .build();
    }

    @Transactional(readOnly = true)
    public ReportDto getYearlyReport(Long userId, int year) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // [start, end) 상한 제외 규약
        LocalDateTime start = LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime end   = LocalDate.of(year + 1, 1, 1).atStartOfDay();

        long totalPagesL   = readingLogRepository.sumPagesByUserIdAndPeriod(user.getId(), start, end);
        long totalMinutesL = readingLogRepository.sumMinutesByUserIdAndPeriod(user.getId(), start, end);

        // ✅ 리뷰 수: [start, end) 규약의 새 메서드 사용
        long completedReviews = reviewRepository.countByUserIdAndCreatedAtRange(user.getId(), start, end);

        int daysInYear = LocalDate.of(year, 1, 1).lengthOfYear();
        int averageDailyMinutes = (daysInYear > 0)
                ? Math.toIntExact(Math.round((double) totalMinutesL / daysInYear))
                : 0;

        List<BadgeDto> badges = badgeRepository
                .findByUserIdAndCreatedAtBetween(user.getId(), start, end.minusNanos(1))
                .stream()
                .map(BadgeDto::from)
                .collect(Collectors.toList());

        int experience = user.getExperience();
        int level      = user.getLevel();

        return ReportDto.builder()
                .totalPages(Math.toIntExact(totalPagesL))
                .totalMinutes(Math.toIntExact(totalMinutesL))
                .completedReviews(completedReviews)
                .averageDailyMinutes(averageDailyMinutes)
                .badges(badges)
                .experience(experience)
                .level(level)
                .build();
    }
}
