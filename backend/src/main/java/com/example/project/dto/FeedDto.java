package com.example.project.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedDto {

    private Long reviewId;                 // 리뷰 ID
    private Long userId;
    private String tagId;
    private String nickname;
    private String username;               // 작성자 닉네임 (사용 중이면 유지)
    private String userProfileImage;       // 작성자 프로필 이미지 URL
    private String content;                // 리뷰 내용
    private Integer rating;                // 별점
    private List<String> reviewImageUrls;  // 리뷰 이미지 URL 리스트
    private LocalDateTime createdAt;       // 작성 시간

    private int likeCount;                 // 좋아요 수
    private int commentCount;              // 댓글 수
    private List<String> hashtags;         // 해시태그
    
    private boolean likedByMe;
private List<LikerDto> likedUsers;

    // 책 정보
    private Long bookId;
    private String bookTitle;

    // 공유 링크
    private String shareUrl;
}
