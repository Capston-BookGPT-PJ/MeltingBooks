package com.example.project.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDto {

    private Long commentId;

    // 작성자 정보
    private Long userId;
    private String nickname;          // 닉네임
    private String tagId;             // 태그 아이디
    private String userProfileImage;  // 프로필 이미지

    // 내용/메타
    private String content;
    private LocalDateTime createdAt;
    private int likeCount;
    private int replyCount;

    // 좋아요 관련
    private boolean likedByMe;        // 내가 좋아요 눌렀는지
    private List<LikerDto> likedUsers;// 좋아요 누른 유저 목록(최신순 등)

}
