package com.example.project.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "group_goals")
public class GroupGoal {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "goal_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate; // 포함

    // ✅ 기간 "총합" 목표치
    @Column(name = "target_books", nullable = false)
    private int targetBooks;

    @Column(name = "target_reviews", nullable = false)
    private int targetReviews;

    @Column(name = "target_minutes", nullable = false)
    private int targetMinutes;

    // ✅ 진행 누적치(캐시)
    @Column(name = "completed_books", nullable = false)
    private int completedBooks;

    @Column(name = "completed_reviews", nullable = false)
    private int completedReviews;

    @Column(name = "completed_minutes", nullable = false)
    private int completedMinutes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    public enum Status {
        ACTIVE, COMPLETED, EXPIRED, CANCELLED
    }

    public boolean isActiveOn(LocalDate date) {
        return status == Status.ACTIVE && !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    public void updateStatusByProgress(LocalDate today) {
        boolean finished =
                (targetBooks   > 0 ? completedBooks   >= targetBooks   : true) &&
                (targetReviews > 0 ? completedReviews >= targetReviews : true) &&
                (targetMinutes > 0 ? completedMinutes >= targetMinutes : true);

        if (finished) {
            this.status = Status.COMPLETED;
        } else if (today.isAfter(endDate)) {
            this.status = Status.EXPIRED;
        } else if (this.status == null) {
            this.status = Status.ACTIVE;
        }
    }
}
