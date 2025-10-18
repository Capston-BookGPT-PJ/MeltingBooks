package com.example.project.entity;

import com.example.project.enums.ExpEventType;
import javax.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "exp_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpEventType eventType;

    @Column(nullable = false)
    private int points;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 편의 생성 메서드
    public static ExpEvent of(User user, ExpEventType eventType) {
        return ExpEvent.builder()
                .user(user)
                .eventType(eventType)
                .points(eventType.getPoints())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
