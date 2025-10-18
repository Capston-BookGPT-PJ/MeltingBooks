package com.example.project.entity;

import com.example.project.enums.BadgeTier;
import com.example.project.enums.BadgeType;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "badge_histories", indexes = {
        @Index(name="idx_badge_hist_user_type", columnList = "user_id,badge_type")
})
public class BadgeHistory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "badge_type", nullable=false, length=50)
    private BadgeType badgeType;

    @Enumerated(EnumType.STRING)
    @Column(name="from_tier", length=20)
    private BadgeTier fromTier;

    @Enumerated(EnumType.STRING)
    @Column(name="to_tier", length=20, nullable=false)
    private BadgeTier toTier;

    @Column(name="reason", length=255)
    private String reason;

    @Column(
        name = "created_at",
        nullable = false,
        updatable = false,
        insertable = false,
        columnDefinition = "DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6)"
    )
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}

