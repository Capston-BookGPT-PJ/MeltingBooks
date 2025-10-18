package com.example.project.dto;

import com.example.project.entity.GroupComment;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupCommentDto {
    private Long id;
    private Long postId;
    private Long userId;
    private String nickname;
    private String profileImageUrl;
    private String content;
    private LocalDateTime createdAt;

    public static GroupCommentDto from(GroupComment c) {
        return GroupCommentDto.builder()
                .id(c.getId())
                .postId(c.getPost().getId())
                .userId(c.getUser().getId())
                .nickname(c.getUser().getNickname())
                .profileImageUrl(c.getUser().getProfileImageUrl())
                .content(c.getContent())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
