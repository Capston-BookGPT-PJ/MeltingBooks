package com.example.project.dto;

import com.example.project.entity.GroupGoal;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupGoalCardDto {
    private Long goalId;
    private Long groupId;

    private String title;
    private String description;

    private LocalDate startDate;
    private LocalDate endDate;

    private int targetBooks;
    private int completedBooks;
    private int targetReviews;
    private int completedReviews;
    private int targetMinutes;
    private int completedMinutes;

    private String status;           // ACTIVE / COMPLETED / EXPIRED
    private int progressBooks;       // %
    private int progressReviews;     // %
    private int progressMinutes;     // %

    public static GroupGoalCardDto fromEntity(GroupGoal g) {
        return GroupGoalCardDto.builder()
                .goalId(g.getId())
                .groupId(g.getGroup().getId())
                .title(g.getTitle())
                .description(g.getDescription())
                .startDate(g.getStartDate())
                .endDate(g.getEndDate())
                .targetBooks(n(g.getTargetBooks()))
                .completedBooks(n(g.getCompletedBooks()))
                .targetReviews(n(g.getTargetReviews()))
                .completedReviews(n(g.getCompletedReviews()))
                .targetMinutes(n(g.getTargetMinutes()))
                .completedMinutes(n(g.getCompletedMinutes()))
                .status(g.getStatus().name())
                .progressBooks(percent(n(g.getCompletedBooks()), n(g.getTargetBooks())))
                .progressReviews(percent(n(g.getCompletedReviews()), n(g.getTargetReviews())))
                .progressMinutes(percent(n(g.getCompletedMinutes()), n(g.getTargetMinutes())))
                .build();
    }

    private static int n(Integer v) { return v == null ? 0 : v; }
    private static int percent(int done, int target) {
        return target > 0 ? Math.min(100, (int)Math.floor(done * 100.0 / target)) : 0;
    }
}
