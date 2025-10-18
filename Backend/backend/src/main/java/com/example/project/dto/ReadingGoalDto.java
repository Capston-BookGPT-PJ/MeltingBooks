package com.example.project.dto;

import com.example.project.entity.ReadingGoal;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReadingGoalDto {
    private Long id;
    private Long userId;

    private ReadingGoal.GoalType goalType;

    private Integer targetBooks;
    private Integer completedBooks;

    private Integer targetReviews;
    private Integer completedReviews;

    private Integer targetMinutes;
    private Integer completedMinutes;

    private Double bookProgress;
    private Double reviewProgress;
    private Double timeProgress;

    private LocalDate startDate;
    private LocalDate endDate;
    private Integer year;
    private Integer month;

    // ✅ 기간 내 완독한 책 목록
    private List<BookBriefDto> books;

    private String booksProgressText;
    private String reviewsProgressText;
    private String timeProgressText;

    public static ReadingGoalDto fromEntity(ReadingGoal g) {
        int tb = n(g.getTargetBooks());
        int cb = n(g.getCompletedBooks());
        int tr = n(g.getTargetReviews());
        int cr = n(g.getCompletedReviews());
        int tm = n(g.getTargetMinutes());
        int cm = n(g.getCompletedMinutes());

        return ReadingGoalDto.builder()
                .id(g.getId())
                .userId(g.getUser().getId())
                .goalType(g.getGoalType())
                .targetBooks(g.getTargetBooks())
                .completedBooks(g.getCompletedBooks())
                .targetReviews(g.getTargetReviews())
                .completedReviews(g.getCompletedReviews())
                .targetMinutes(g.getTargetMinutes())
                .completedMinutes(g.getCompletedMinutes())
                .bookProgress(g.getBookProgress())
                .reviewProgress(g.getReviewProgress())
                .timeProgress(g.getTimeProgress())
                .startDate(g.getStartDate())
                .endDate(g.getEndDate())
                .year(g.getYear())
                .month(g.getMonth())
                // books는 서비스에서 채움
                .books(null)
                .booksProgressText(cb + "권/" + tb + "권")
                .reviewsProgressText(cr + "개/" + tr + "개")
                .timeProgressText(minutesToText(cm) + "/" + minutesToText(tm))
                .build();
    }

    private static int n(Integer v) { return v == null ? 0 : v; }

    private static String minutesToText(int m) {
        int h = m / 60;
        int mm = m % 60;
        if (h > 0 && mm > 0) return h + "시간 " + mm + "분";
        if (h > 0) return h + "시간";
        return mm + "분";
    }
}
