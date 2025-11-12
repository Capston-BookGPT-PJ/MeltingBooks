package com.example.project.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "reading_logs")
public class ReadingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="book_id", nullable = false)
    private Book book;

    @Column(name = "pages_read", nullable = false)
    private int pagesRead;

    @Column(name = "minutes_read", nullable = false)
    private int minutesRead;

    @Column(name = "read_at", nullable = false)
    private LocalDateTime readAt;

    @Column(name = "finished", nullable = false)
    private boolean finished;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @PrePersist
    public void prePersist() {
        if (readAt == null) readAt = LocalDateTime.now();
        if (finished && finishedAt == null) finishedAt = readAt;
    }
}
