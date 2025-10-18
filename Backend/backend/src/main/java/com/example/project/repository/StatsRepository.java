package com.example.project.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Optional;

public interface StatsRepository {

    long countFinishedBooks(Long userId, LocalDate from, LocalDate to);
    long countTotalFinishedBooks(Long userId);

    long countFinishedBooksByGenre(Long userId, String genre);

    long countAchievedMonthlyGoals(Long userId);
    boolean isMonthlyGoalAchieved(Long userId, YearMonth ym);

    Optional<Integer> getYearlyTarget(Long userId, int year);
    long getYearlyProgress(Long userId, int year);

    long countReviews(Long userId);
    long countMonthlyReviews(Long userId, int year, int month);

    long countCommentsOnOthersReviews(Long userId);
    long countMonthlyCommentsOnOthersReviews(Long userId, int year, int month);

    long countAchievedGroupGoals(Long userId);
    long countGroupReviews(Long userId);
    long countGroupCommentsOnOthersReviews(Long userId);
    long countLikesReceivedForMyGroupReviews(Long userId);

    // ✅ [신규] 서로 다른 장르 중, 최소 N권 이상 완독한 장르의 개수
    long countGenresWithMinFinished(Long userId, int minCount);
}