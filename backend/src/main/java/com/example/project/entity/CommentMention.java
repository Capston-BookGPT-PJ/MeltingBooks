package com.example.project.entity;

import lombok.*;
import javax.persistence.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "comment_mention")
public class CommentMention {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mention_id")
    private Long mentionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentioned_user_id", nullable = false)
    private User mentionedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conment_id", nullable = false)
    private Comment comment;
}
