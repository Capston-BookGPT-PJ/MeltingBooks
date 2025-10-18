package com.example.project.dto;

import com.example.project.entity.Review;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDto {

    private Long reviewId;
    private String content;
    private List<String> reviewImageUrls;
    private Integer rating;

    private Long userId;
    private Long bookId;

    // 사용자 표시용
    private String nickname;
    private String userProfileImage;
    private String tagId;

    private String createdAt;
    private String updatedAt;
    private List<String> hashtags;

    private String shareUrl;

    // ✅ like 영역 (기본값 보장)
    @Builder.Default
    private long likeCount = 0L;

    @Builder.Default
    private boolean likedByMe = false;

    @Builder.Default
    private List<LikerDto> likedUsers = List.of();

    /** 기존 사용처 호환용 */
    public static ReviewDto from(Review review) { return from(review, null); }

    /** 공유 링크 포함 기본 변환 */
    public static ReviewDto from(Review review, String shareUrl) {
        var user = review.getUser();

        return ReviewDto.builder()
                .reviewId(review.getId())
                .content(review.getContent())
                .reviewImageUrls(review.getReviewImageUrls())
                .rating(review.getRating())
                .userId(user != null ? user.getId() : null)
                .bookId(review.getBook() != null ? review.getBook().getId() : null)

                .nickname(user != null ? user.getNickname() : null)
                .userProfileImage(user != null ? user.getProfileImageUrl() : null)
                .tagId(user != null ? sanitizeTagId(user.getTagId()) : null)

                .createdAt(review.getCreatedAt() != null ? review.getCreatedAt().toString() : null)
                .updatedAt(review.getUpdatedAt() != null ? review.getUpdatedAt().toString() : null)
                .hashtags(review.getHashtags() != null
                        ? review.getHashtags().stream()
                            .map(rh -> "#" + rh.getHashtag().getTagText())
                            .collect(Collectors.toList())
                        : List.of())
                .shareUrl(shareUrl)
                .build();
    }

    /** ✅ 좋아요 정보 포함 조립 */
    public static ReviewDto fromWithLikeInfo(
            Review review,
            String shareUrl,
            long likeCount,
            boolean likedByMe,
            List<LikerDto> likedUsers
    ) {
        ReviewDto dto = from(review, shareUrl);
        dto.setLikeCount(likeCount);
        dto.setLikedByMe(likedByMe);
        dto.setLikedUsers(likedUsers != null ? likedUsers : List.of());
        return dto;
    }

    // DB에 '@id' 형태 대비
    private static String sanitizeTagId(String raw) {
        if (raw == null) return null;
        String t = raw.trim();
        return t.startsWith("@") ? t.substring(1) : t;
    }
}
