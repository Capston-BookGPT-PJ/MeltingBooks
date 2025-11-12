package com.example.project.dto;

import com.example.project.entity.GroupPost;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class GroupPostDto {
    private Long id;
    private Long groupId;
    private Long userId;
    private String postType;
    private String title;
    private String content;
    private List<String> imageUrls;
    private LocalDateTime createdAt;

    private int likeCount;
    private int commentCount;
    private boolean likedByMe;
    private List<LikerDto> likedUsers;

    public static GroupPostDto fromEntity(GroupPost post) {
        return GroupPostDto.builder()
                .id(post.getId())
                .groupId(post.getGroup().getId())
                .userId(post.getUser().getId())
                .postType(post.getPostType().name())
                .title(post.getTitle())
                .content(post.getContent())
                .imageUrls(
                        post.getImages() == null ? List.of()
                                : post.getImages().stream()
                                    .sorted((a, b) -> Integer.compare(
                                            a.getSortOrder() == null ? 0 : a.getSortOrder(),
                                            b.getSortOrder() == null ? 0 : b.getSortOrder()))
                                    .map(img -> img.getImageUrl())
                                    .collect(Collectors.toList())
                )
                .createdAt(post.getCreatedAt())
                .build();
    }

    public static GroupPostDto fromEntityWithLikes(GroupPost post,
                                                   int likeCount,
                                                   boolean likedByMe,
                                                   List<LikerDto> likedUsers) {
        GroupPostDto dto = fromEntity(post);
        dto.setLikeCount(likeCount);
        dto.setLikedByMe(likedByMe);
        dto.setLikedUsers(likedUsers);
        return dto;
    }
    
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }

}
