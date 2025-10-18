    package com.example.project.entity;
    
    import com.fasterxml.jackson.annotation.JsonBackReference;
    import lombok.*;
    import javax.persistence.*;
    import java.time.LocalDateTime;
    
    @Entity
    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor @Builder
    @Table(name = "follows")
    public class Follow {
        @Id 
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "follow_id")
        private Long followId;
    
        @Column(name = "created_at", updatable = false)
        private LocalDateTime createdAt;
    
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "follower_id", nullable = false)
        @JsonBackReference
        private User follower;
    
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "following_id", nullable = false)
        @JsonBackReference
        private User following;
    
        @PrePersist
        public void onCreate() {
            this.createdAt = LocalDateTime.now();
        }
    
        public Follow(User follower, User following) {
            this.follower = follower;
            this.following = following;
            this.createdAt = LocalDateTime.now();
        }
    }
    