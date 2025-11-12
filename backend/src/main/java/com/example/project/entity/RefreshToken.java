package com.example.project.entity;

import javax.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "refresh_tokens",
       indexes = {
           @Index(name = "idx_refresh_token_token", columnList = "token", unique = true),
           @Index(name = "idx_refresh_token_user", columnList = "user_id")
       })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class RefreshToken {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 토큰 원문(필요하면 해시 저장 권장)
    @Column(nullable = false, unique = true, length = 512)
    private String token;

    // 누가 발급 받았는지
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 만료시각(Unix epoch millis 또는 Instant)
    @Column(nullable = false)
    private Instant expiresAt;

    // 회전/로그아웃 시 true
    @Column(nullable = false)
    private boolean revoked;

    // 토큰 재사용 공격 방지용(회전 시 새 토큰 id 문자열 저장 가능)
    private String replacedByToken;

    // 감사 로그용
    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
