package com.example.project.dto;

import com.example.project.entity.ReadingLog;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReadingLogDto {
    private Long id;
    private Long userId;
    private Long bookId;
    private int pagesRead;
    private int minutesRead;
    private LocalDateTime readAt;

    /** ✅ 완독 여부(요청/응답 둘 다에서 사용) */
    private Boolean isFinished;

    public static ReadingLogDto fromEntity(ReadingLog e) {
    return ReadingLogDto.builder()
            .id(e.getId())
            .userId(e.getUser() != null ? e.getUser().getId() : null)
            .bookId(e.getBook() != null ? e.getBook().getId() : null)
            .pagesRead(e.getPagesRead())
            .minutesRead(e.getMinutesRead())
            .readAt(e.getReadAt())
            .isFinished(e.isFinished())
            .build();
}
}

