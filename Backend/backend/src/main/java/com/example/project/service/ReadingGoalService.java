package com.example.project.service;

import com.example.project.dto.BookBriefDto;
import com.example.project.dto.ReadingGoalDto;
import com.example.project.entity.Book;
import com.example.project.entity.ReadingGoal;
import com.example.project.entity.ReadingGoal.GoalType;
import com.example.project.entity.User;
import com.example.project.exception.UserNotFoundException;
import com.example.project.repository.ReadingGoalRepository;
import com.example.project.repository.ReadingLogRepository;
import com.example.project.repository.ReviewRepository;
import com.example.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReadingGoalService {

    private final ReadingGoalRepository goalRepository;
    private final UserRepository userRepository;
    private final ReadingLogRepository readingLogRepository;
    private final ReviewRepository reviewRepository; // ✅ 추가

    @Transactional
    public ReadingGoalDto createGoal(Long userId, ReadingGoalDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. ID=" + userId));

        int year = (dto.getStartDate() != null) ? dto.getStartDate().getYear() : LocalDate.now().getYear();
        Integer month = (dto.getGoalType() == GoalType.MONTHLY)
                ? ((dto.getStartDate() != null) ? dto.getStartDate().getMonthValue() : LocalDate.now().getMonthValue())
                : null;

        ReadingGoal goal = ReadingGoal.builder()
                .user(user)
                .goalType(dto.getGoalType())
                .targetBooks(n(dto.getTargetBooks()))
                .completedBooks(0)
                .targetReviews(n(dto.getTargetReviews()))
                .completedReviews(0)
                .targetMinutes(n(dto.getTargetMinutes()))
                .completedMinutes(0)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .year(year)
                .month(month)
                .bookProgress(0)
                .reviewProgress(0)
                .timeProgress(0)
                .build();

        goal.updateProgress();
        return ReadingGoalDto.fromEntity(goalRepository.save(goal));
    }

    @Transactional
    public ReadingGoalDto updateGoalById(Long userId, Long goalId, ReadingGoalDto dto) {
        return updateGoal(goalId, userId, dto);
    }

    /* =========================
       /me: books 포함 전체 조회 — 로그/리뷰 "원천 데이터"로 재계산
       ========================= */
 @Transactional(readOnly = true)
public List<ReadingGoalDto> getGoalsByUserWithBooks(Long userId) {
    List<ReadingGoal> goals = goalRepository.findByUserId(userId);
    List<ReadingGoalDto> out = new ArrayList<>();

    for (ReadingGoal g : goals) {
        PeriodRange pr = resolvePeriod(g); // [start, endExclusive)

        // 완독 도서/시간: 원천(ReadingLog)
        List<Book> finished = readingLogRepository
                .findFinishedBooksByUserAndPeriod(userId, pr.start, pr.endExclusive);
        long minutes = readingLogRepository
                .sumMinutesByUserIdAndPeriod(userId, pr.start, pr.endExclusive);

        // ✅ 리뷰: 원천(Review) — 실제 작성 수, 상한 없음
        long reviews = reviewRepository
                .countByUserIdAndCreatedAtRange(userId, pr.start, pr.endExclusive);

        ReadingGoalDto dto = ReadingGoalDto.fromEntity(g);
        dto.setBooks(finished.stream().map(BookBriefDto::from).collect(Collectors.toList()));
        dto.setCompletedBooks(dto.getBooks().size());
        dto.setCompletedMinutes(Math.toIntExact(minutes));
        dto.setCompletedReviews(Math.toIntExact(reviews)); // ★ 타겟 초과 시 초과분 그대로

        int targetBooks   = dto.getTargetBooks()   == null ? 0 : dto.getTargetBooks();
        int targetMinutes = dto.getTargetMinutes() == null ? 0 : dto.getTargetMinutes();
        int targetReviews = dto.getTargetReviews() == null ? 0 : dto.getTargetReviews();

        dto.setBookProgress(targetBooks == 0 ? 0.0 :
                Math.min(100.0, dto.getCompletedBooks() * 100.0 / targetBooks));
        dto.setTimeProgress(targetMinutes == 0 ? 0.0 :
                Math.min(100.0, minutes * 100.0 / targetMinutes));

        // ✅ 진행률만 100% 캡
        dto.setReviewProgress(targetReviews == 0 ? 0.0 :
                Math.min(100.0, reviews * 100.0 / targetReviews));

        out.add(dto);
    }
    return out;
}


    /* =========================
       단건 조회 — 로그/리뷰 "원천 데이터"로 재계산
       ========================= */
    @Transactional(readOnly = true)
    public ReadingGoalDto getGoal(Long id) {
        ReadingGoal goal = goalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("목표를 찾을 수 없습니다. ID=" + id));

        PeriodRange pr = resolvePeriod(goal); // [start, endExclusive)

        List<Book> finishedBooks = readingLogRepository
                .findFinishedBooksByUserAndPeriod(goal.getUser().getId(), pr.start, pr.endExclusive);
        long minutes = readingLogRepository
                .sumMinutesByUserIdAndPeriod(goal.getUser().getId(), pr.start, pr.endExclusive);

        // ✅ 리뷰 실제 작성 수 (상한 없음)
        long reviews = reviewRepository
                .countByUserIdAndCreatedAtRange(goal.getUser().getId(), pr.start, pr.endExclusive);

        ReadingGoalDto dto = ReadingGoalDto.fromEntity(goal);
        dto.setBooks(finishedBooks.stream().map(BookBriefDto::from).collect(Collectors.toList()));
        dto.setCompletedBooks(dto.getBooks().size());
        dto.setCompletedMinutes(Math.toIntExact(minutes));
        dto.setCompletedReviews(Math.toIntExact(reviews)); // ✅ 타겟 초과 표시 OK

        int targetBooks   = dto.getTargetBooks()   == null ? 0 : dto.getTargetBooks();
        int targetMinutes = dto.getTargetMinutes() == null ? 0 : dto.getTargetMinutes();
        int targetReviews = dto.getTargetReviews() == null ? 0 : dto.getTargetReviews();

        dto.setBookProgress(targetBooks == 0 ? 0.0 :
                Math.min(100.0, dto.getCompletedBooks() * 100.0 / targetBooks));
        dto.setTimeProgress(targetMinutes == 0 ? 0.0 :
                Math.min(100.0, minutes * 100.0 / targetMinutes));
        dto.setReviewProgress(targetReviews == 0 ? 0.0 :
                Math.min(100.0, reviews * 100.0 / targetReviews)); // ✅ 100% 캡

        return dto;
    }

    /* =========================
       수정/삭제 (기존 로직 그대로)
       ========================= */
    @Transactional
    public ReadingGoalDto updateGoalByPeriod(Long userId, ReadingGoalDto dto) {
        if (dto.getGoalType() == null || dto.getYear() == null) {
            throw new IllegalArgumentException("goalType과 year는 필수입니다.");
        }
        ReadingGoal goal = goalRepository.findByUserIdAndGoalTypeAndYearAndMonth(
                userId, dto.getGoalType(), dto.getYear(), dto.getMonth())
                .orElseThrow(() -> new RuntimeException("목표를 찾을 수 없습니다."));

        goal.setTargetBooks(n(dto.getTargetBooks()));
        goal.setTargetReviews(n(dto.getTargetReviews()));
        goal.setTargetMinutes(n(dto.getTargetMinutes()));
        goal.setStartDate(dto.getStartDate());
        goal.setEndDate(dto.getEndDate());
        goal.setYear(dto.getYear());
        goal.setMonth(dto.getGoalType() == GoalType.MONTHLY ? dto.getMonth() : null);

        goal.updateProgress();
        return ReadingGoalDto.fromEntity(goalRepository.save(goal));
    }

    @Transactional
    public void deleteGoalByPeriod(Long userId, GoalType type, int year, Integer month) {
        ReadingGoal goal = goalRepository.findByUserIdAndGoalTypeAndYearAndMonth(userId, type, year, month)
                .orElseThrow(() -> new RuntimeException("목표를 찾을 수 없습니다."));
        goalRepository.delete(goal);
    }

    @Transactional
    public void deleteMyGoalById(Long userId, Long goalId) {
        ReadingGoal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("목표를 찾을 수 없습니다. ID=" + goalId));
        if (goal.getUser() == null || !goal.getUser().getId().equals(userId)) {
            throw new IllegalStateException("삭제 권한이 없습니다.");
        }
        goalRepository.delete(goal);
    }

    @Transactional
    public ReadingGoalDto updateGoal(Long id, Long userId, ReadingGoalDto dto) {
        ReadingGoal goal = goalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("목표를 찾을 수 없습니다. ID=" + id));
        if (goal.getUser() == null || !goal.getUser().getId().equals(userId)) {
            throw new IllegalStateException("수정 권한이 없습니다.");
        }

        goal.setGoalType(dto.getGoalType());
        goal.setTargetBooks(n(dto.getTargetBooks()));
        goal.setTargetReviews(n(dto.getTargetReviews()));
        goal.setTargetMinutes(n(dto.getTargetMinutes()));
        goal.setStartDate(dto.getStartDate());
        goal.setEndDate(dto.getEndDate());
        goal.setYear(dto.getStartDate() != null ? dto.getStartDate().getYear() : goal.getYear());
        goal.setMonth(dto.getGoalType() == GoalType.MONTHLY
                ? (dto.getStartDate() != null ? dto.getStartDate().getMonthValue() : goal.getMonth())
                : null);

        goal.updateProgress();
        return ReadingGoalDto.fromEntity(goalRepository.save(goal));
    }

    /* =========================
       진행도 조작/이벤트 훅 (기존 유지)
       ========================= */
    @Transactional
    public ReadingGoalDto completeBook(Long id) {
        ReadingGoal goal = goalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("목표를 찾을 수 없습니다. ID=" + id));
        goal.completeBook();
        return ReadingGoalDto.fromEntity(goalRepository.save(goal));
    }

    @Transactional
    public ReadingGoalDto completeReview(Long id) {
        ReadingGoal goal = goalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("목표를 찾을 수 없습니다. ID=" + id));
        goal.completeReview();
        return ReadingGoalDto.fromEntity(goalRepository.save(goal));
    }

    @Transactional
    public ReadingGoalDto addReadingTime(Long id, int minutes) {
        ReadingGoal goal = goalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("목표를 찾을 수 없습니다. ID=" + id));
        goal.addReadingTime(minutes);
        return ReadingGoalDto.fromEntity(goalRepository.save(goal));
    }

    @Transactional
    public void onReviewCreated(Long userId) {
        LocalDate today = LocalDate.now();
        int year  = today.getYear();
        int month = today.getMonthValue();
        List<ReadingGoal> goals = goalRepository.findActiveGoals(userId, today, year, month);
        for (ReadingGoal g : goals) g.completeReview();
    }

    @Transactional
    public void completeBookByUserOnDate(Long userId, LocalDate date) {
        int year  = date.getYear();
        int month = date.getMonthValue();
        List<ReadingGoal> goals = goalRepository.findActiveGoals(userId, date, year, month);
        for (ReadingGoal g : goals) g.completeBook();
    }

    @Transactional
    public void addReadingTimeByUserOnDate(Long userId, int minutes, LocalDate date) {
        int year  = date.getYear();
        int month = date.getMonthValue();
        List<ReadingGoal> goals = goalRepository.findActiveGoals(userId, date, year, month);
        for (ReadingGoal g : goals) g.addReadingTime(minutes);
    }

    @Transactional(readOnly = true)
    public ReadingGoalDto recomputeBooks(Long goalId) { return getGoal(goalId); }

    @Transactional
    public void completeBookByUser(Long userId) { completeBookByUserOnDate(userId, LocalDate.now()); }

    @Transactional
    public void completeReviewByUser(Long userId) { onReviewCreated(userId); }

    @Transactional
    public void addReadingTimeByUser(Long userId, int minutes) { addReadingTimeByUserOnDate(userId, minutes, LocalDate.now()); }

    /* =========================
       내부 유틸/기간 (끝 미포함 모델)
       ========================= */
    private static int n(Integer v) { return v == null ? 0 : v; }

    static class PeriodRange {
        final LocalDateTime start;         // inclusive
        final LocalDateTime endExclusive;  // exclusive
        PeriodRange(LocalDateTime s, LocalDateTime eEx) { this.start = s; this.endExclusive = eEx; }
    }

    PeriodRange resolvePeriod(ReadingGoal g) {
        if (g.getStartDate() != null && g.getEndDate() != null) {
            return new PeriodRange(
                    g.getStartDate().atStartOfDay(),
                    g.getEndDate().plusDays(1).atStartOfDay()  // ★ 끝 미포함
            );
        }
        int year = (g.getYear() > 0) ? g.getYear() : LocalDate.now().getYear();

        if (g.getGoalType() == GoalType.MONTHLY) {
            int monthVal = (g.getMonth() != null && g.getMonth() > 0)
                    ? g.getMonth() : LocalDate.now().getMonthValue();
            YearMonth ym = YearMonth.of(year, monthVal);
            LocalDate s = ym.atDay(1);
            LocalDate eNext = ym.plusMonths(1).atDay(1); // 다음달 1일
            return new PeriodRange(s.atStartOfDay(), eNext.atStartOfDay());
        } else { // YEARLY 등
            LocalDate s = LocalDate.of(year, 1, 1);
            LocalDate eNext = LocalDate.of(year + 1, 1, 1); // 다음해 1/1
            return new PeriodRange(s.atStartOfDay(), eNext.atStartOfDay());
        }
    }
}
