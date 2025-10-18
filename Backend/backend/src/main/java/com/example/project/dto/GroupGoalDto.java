// src/main/java/com/example/project/dto/GroupGoalDto.java
package com.example.project.dto;

import com.example.project.entity.GroupGoal;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupGoalDto {
    private Long id;
    private Long groupId;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private int targetBooks;
    private int targetReviews;
    private int targetMinutes;
    private int completedBooks;
    private int completedReviews;
    private int completedMinutes;
    private String status;

    public static GroupGoalDto fromEntity(GroupGoal e) {
        return GroupGoalDto.builder()
                .id(e.getId())
                .groupId(e.getGroup().getId())
                .title(e.getTitle())
                .description(e.getDescription())
                .startDate(e.getStartDate())
                .endDate(e.getEndDate())
                .targetBooks(e.getTargetBooks())
                .targetReviews(e.getTargetReviews())
                .targetMinutes(e.getTargetMinutes())
                .completedBooks(e.getCompletedBooks())
                .completedReviews(e.getCompletedReviews())
                .completedMinutes(e.getCompletedMinutes())
                .status(e.getStatus() != null ? e.getStatus().name() : null)
                .build();
    }
}
