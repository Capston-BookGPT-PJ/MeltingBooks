package com.example.project.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "polls")
public class Poll {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "poll_id")
    private Long pollId;

    private String title;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
