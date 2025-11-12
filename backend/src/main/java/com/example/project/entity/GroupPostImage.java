package com.example.project.entity;

import lombok.*;
import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "group_post_images")
public class GroupPostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private GroupPost post;

    @Column(nullable = false, length = 1000)
    private String imageUrl;

    // 정렬이 필요하면 순번 칼럼 추가
    @Column(name = "sort_order")
    private Integer sortOrder;
}
