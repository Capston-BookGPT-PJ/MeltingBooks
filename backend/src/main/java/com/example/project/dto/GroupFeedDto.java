package com.example.project.dto;

import com.example.project.entity.GroupPost;
import com.example.project.entity.GroupPostImage;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class GroupFeedDto {
    private Long id;
    private Long groupId;
    private Long userId;
    private String postType;
    private String title;
    private String content;
    private List<String> imageUrls;
    private LocalDateTime createdAt;

    private int likeCount;
    private boolean likedByMe;
    private List<LikerDto> likedUsers;

    public static GroupFeedDto baseFrom(GroupPost post) {
        return GroupFeedDto.builder()
                .id(post.getId())
                .groupId(post.getGroup().getId())
                .userId(post.getUser().getId())
                .postType(post.getPostType().name())
                .title(post.getTitle())
                .content(post.getContent())
                .imageUrls(
                        post.getImages() == null ? List.of() :
                                post.getImages().stream()
                                        .sorted((a,b) -> Integer.compare(
                                                a.getSortOrder() == null ? 0 : a.getSortOrder(),
                                                b.getSortOrder() == null ? 0 : b.getSortOrder()))
                                        .map(GroupPostImage::getImageUrl)
                                        .collect(Collectors.toList())
                )
                .createdAt(post.getCreatedAt())
                .build();
    }

    // 좋아요 정보 주입
    public GroupFeedDto withLikes(int likeCount, boolean likedByMe, List<LikerDto> likedUsers) {
        this.likeCount = likeCount;
        this.likedByMe = likedByMe;
        this.likedUsers = likedUsers;
        return this;
    }
}
