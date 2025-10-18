package com.example.project.entity;

import com.example.project.enums.BadgeTier;
import com.example.project.enums.BadgeType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Table(
    name = "badges",
    uniqueConstraints = @UniqueConstraint(name = "uk_badge_user_type", columnNames = {"user_id","badge_type"})
)
public class Badge {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false)
    @JsonBackReference
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name="badge_type", nullable=false, length=50)
    private BadgeType badgeType;

    @Enumerated(EnumType.STRING)
    @Column(name="tier", nullable=false, length=20)
    private BadgeTier tier;

    @Column(name="image_url", length=500)
    private String imageUrl;

    @Column(
        name = "created_at",
        nullable = false,
        updatable = false,
        insertable = false,
        columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
    )
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
