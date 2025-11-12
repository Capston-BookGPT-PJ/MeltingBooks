package com.example.project.entity;

import lombok.*;
import javax.persistence.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Table(
  name = "review_hashtags",
  uniqueConstraints = @UniqueConstraint(columnNames = {"review_id","hashtag_id"})
)
public class ReviewHashtag {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_hashtag_id")
    private Long reviewHashtagId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hashtag_id", nullable = false)
    private Hashtag hashtag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;
}
