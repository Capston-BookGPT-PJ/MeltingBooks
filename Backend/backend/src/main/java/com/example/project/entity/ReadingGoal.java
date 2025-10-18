package com.example.project.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reading_goals")
public class ReadingGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "goal_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal_type", nullable = false)
    private GoalType goalType;

    @Column(nullable = false)
    private int year;

    @Column
    private Integer month;

    @Column(name = "target_books", nullable = false)
    private int targetBooks;

    @Column(name = "completed_books", nullable = false)
    private int completedBooks;

    @Column(name = "target_reviews")
    private int targetReviews;

    @Column(name = "completed_reviews", nullable = false)
    private int completedReviews;

    @Column(name = "target_minutes", nullable = false)
    private int targetMinutes;

    @Column(name = "completed_minutes", nullable = false)
    private int completedMinutes;

    @Column(name = "book_progress", nullable = false)
    private double bookProgress;

    @Column(name = "review_progress", nullable = false)
    private double reviewProgress;

    @Column(name = "time_progress", nullable = false)
    private double timeProgress;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    // (옵션) 동시성 안전을 위한 낙관적 락
    // @Version
    // private Long version;

    public void updateProgress() {
        bookProgress   = pct(completedBooks,   targetBooks);
        reviewProgress = pct(completedReviews, targetReviews);
        timeProgress   = pct(completedMinutes, targetMinutes);

        // 모두 100%면 종료일 스탬프
        if (bookProgress >= 100.0 && reviewProgress >= 100.0 && timeProgress >= 100.0) {
            this.endDate = LocalDate.now();
        }
    }

    private double pct(int done, int target) {
        if (target <= 0) return 0.0;
        double p = ((double) done / (double) target) * 100.0;
        if (p > 100.0) p = 100.0;
        if (p < 0.0)   p = 0.0;
        return p;
    }

    public void completeBook() {
        this.completedBooks = Math.max(0, this.completedBooks + 1);
        updateProgress();
    }

    public void completeReview() {
        this.completedReviews = Math.max(0, this.completedReviews + 1);
        updateProgress();
    }

    public void addReadingTime(int minutes) {
        this.completedMinutes = Math.max(0, this.completedMinutes + Math.max(0, minutes));
        updateProgress();
    }

    public enum GoalType { MONTHLY, YEARLY }
}
