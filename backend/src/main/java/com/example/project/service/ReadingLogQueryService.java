package com.example.project.service;

import com.example.project.dto.ReadingLogDto;
import com.example.project.entity.ReadingLog;
import com.example.project.repository.ReadingLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReadingLogQueryService {

    private final ReadingLogRepository readingLogRepository;

    @Transactional(readOnly = true)
    public List<ReadingLogDto> getLogs(Long userId, LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from, to는 필수입니다. (yyyy-MM-dd)");
        }
        // 날짜 역순이면 자동 보정
        if (from.isAfter(to)) {
            LocalDate tmp = from; from = to; to = tmp;
        }

        // [포함] 범위: from 00:00:00 ~ to 23:59:59.999999999
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end   = to.plusDays(1).atStartOfDay().minusNanos(1);

        List<ReadingLog> logs = readingLogRepository.findLogsInRange(userId, start, end);
        return logs.stream().map(ReadingLogDto::fromEntity).toList();
    }
}
