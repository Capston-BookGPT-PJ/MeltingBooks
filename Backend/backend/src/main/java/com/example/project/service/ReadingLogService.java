package com.example.project.service;

import com.example.project.dto.ReadingLogDto;
import com.example.project.dto.ReadingLogStatsDto;
import com.example.project.entity.Book;
import com.example.project.entity.ReadingLog;
import com.example.project.entity.User;
import com.example.project.repository.BookRepository;
import com.example.project.repository.ReadingLogRepository;
import com.example.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// ✅ 배지
import com.example.project.service.badge.BadgeService;

@Service
@RequiredArgsConstructor
public class ReadingLogService {

    private final ReadingLogRepository logRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    private final ReadingGoalService readingGoalService;
    private final GroupGoalService groupGoalService;

    private final BadgeService badgeService;

    /** 생성 (bookId 필요) */
    @Transactional
    public ReadingLogDto create(Long userId, Long bookId, ReadingLogDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("책을 찾을 수 없습니다."));

        int pages   = Math.max(0, dto.getPagesRead());
        int minutes = Math.max(0, dto.getMinutesRead());
        LocalDateTime readAt = (dto.getReadAt() != null) ? dto.getReadAt() : LocalDateTime.now();
        boolean finished = Boolean.TRUE.equals(dto.getIsFinished());

        ReadingLog log = ReadingLog.builder()
                .user(user)
                .book(book)
                .pagesRead(pages)
                .minutesRead(minutes)
                .readAt(readAt)
                .finished(finished)
                .finishedAt(finished ? readAt : null)
                .build();

        ReadingLog saved = logRepository.save(log);

        // 읽은 날짜 기준으로 목표 반영
        LocalDate day = readAt.toLocalDate();
        if (minutes > 0) {
            readingGoalService.addReadingTimeByUserOnDate(userId, minutes, day);
            groupGoalService.onReadingLogCreated(userId, readAt, minutes);
        }
        if (finished) {
            readingGoalService.completeBookByUserOnDate(userId, day);
        }

        badgeService.evaluateAll(userId);
        return ReadingLogDto.fromEntity(saved);
    }

    /** 유저 전체 로그 조회 (최신순) */
    @Transactional(readOnly = true)
    public List<ReadingLogDto> getByUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        var logs = logRepository.findByUser_IdOrderByReadAtDesc(userId, Pageable.unpaged());
        return logs.stream().map(ReadingLogDto::fromEntity).collect(Collectors.toList());
    }

    /** 수정 — ✅ minutes/readAt 변경분 diff를 목표에 반영 */
    @Transactional
    public ReadingLogDto update(Long logId, ReadingLogDto dto) {
        ReadingLog log = logRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("독서 기록을 찾을 수 없습니다."));

        int prevMinutes = log.getMinutesRead();
        LocalDate prevDay = log.getReadAt() != null ? log.getReadAt().toLocalDate() : LocalDate.now();

        // 적용
        log.setPagesRead(Math.max(0, dto.getPagesRead()));
        log.setMinutesRead(Math.max(0, dto.getMinutesRead()));
        if (dto.getReadAt() != null) {
            log.setReadAt(dto.getReadAt());
        }
        if (dto.getIsFinished() != null) {
            boolean f = Boolean.TRUE.equals(dto.getIsFinished());
            log.setFinished(f);
            log.setFinishedAt(f ? (log.getFinishedAt() != null ? log.getFinishedAt() : log.getReadAt()) : null);
        }

        ReadingLog saved = logRepository.save(log);

        // ✅ diff 반영
        int newMinutes = saved.getMinutesRead();
        LocalDate newDay = saved.getReadAt() != null ? saved.getReadAt().toLocalDate() : prevDay;
        int diff = newMinutes - prevMinutes;

        if (!newDay.equals(prevDay)) {
            // 날짜가 바뀌면: 이전 날짜에서 prevMinutes만큼 빼고, 새 날짜에 newMinutes만큼 더함
            if (prevMinutes != 0) readingGoalService.addReadingTimeByUserOnDate(saved.getUser().getId(), -prevMinutes, prevDay);
            if (newMinutes != 0)  readingGoalService.addReadingTimeByUserOnDate(saved.getUser().getId(),  newMinutes, newDay);
        } else {
            // 같은 날짜면: 차이만 반영
            if (diff != 0) readingGoalService.addReadingTimeByUserOnDate(saved.getUser().getId(), diff, newDay);
        }

        badgeService.evaluateAll(saved.getUser().getId());
        return ReadingLogDto.fromEntity(saved);
    }

    /** 삭제 — ✅ minutes 감소 반영 */
    @Transactional
    public void delete(Long userId, Long logId) {
        ReadingLog log = logRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("독서 기록을 찾을 수 없습니다. ID=" + logId));

        if (log.getUser() == null || !log.getUser().getId().equals(userId)) {
            throw new RuntimeException("권한이 없습니다.");
        }

        int minutes = log.getMinutesRead();
        LocalDate day = log.getReadAt() != null ? log.getReadAt().toLocalDate() : LocalDate.now();

        logRepository.delete(log);

        if (minutes != 0) {
            readingGoalService.addReadingTimeByUserOnDate(userId, -minutes, day);
        }

        badgeService.evaluateAll(userId);
    }

    /** 통계 */
    @Transactional(readOnly = true)
    public ReadingLogStatsDto getStats(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        long totalPagesL     = logRepository.sumPagesByUserId(userId);
        long totalMinutesL   = logRepository.sumMinutesByUserId(userId);
        long completedBooksL = logRepository.countCompletedBooksByUserId(userId);

        LocalDateTime firstLogDateTime = logRepository.findFirstLogDateByUserId(userId);
        LocalDateTime lastLogDateTime  = logRepository.findLastLogDateByUserId(userId);

        return ReadingLogStatsDto.builder()
                .userId(userId)
                .totalPages(Math.toIntExact(totalPagesL))
                .totalMinutes(Math.toIntExact(totalMinutesL))
                .completedBooks(Math.toIntExact(completedBooksL))
                .firstLogDate(firstLogDateTime != null ? firstLogDateTime.toLocalDate() : null)
                .lastLogDate(lastLogDateTime  != null ? lastLogDateTime.toLocalDate()  : null)
                .build();
    }
}
