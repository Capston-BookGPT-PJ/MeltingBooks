package com.example.project.service;

import com.example.project.common.ShareLinkService;   // ✅ 공유 링크
import com.example.project.dto.FeedDto;
import com.example.project.entity.Review;
import com.example.project.repository.CommentRepository;
import com.example.project.repository.FollowRepository;
import com.example.project.repository.LikeRepository;
import com.example.project.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final ReviewRepository reviewRepository;
    private final FollowRepository followRepository;
    private final LikeRepository likeRepository;       // 좋아요 카운트
    private final CommentRepository commentRepository; // 댓글 카운트
    private final ShareLinkService shareLinkService;   // 공유 링크

    @Transactional(readOnly = true)
    public Page<FeedDto> getFeed(Long userId, int page, int size) {

        // 1) 팔로잉한 유저 ID + 본인 ID
        List<Long> followingIds = new ArrayList<>(followRepository.findFollowingIdsByFollowerId(userId));
        if (!followingIds.contains(userId)) {
            followingIds.add(userId);
        }

        // 2) 최신순 페이지 조회
        PageRequest pageable = PageRequest.of(Math.max(0, page), Math.max(1, size),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviewPage = reviewRepository.findByUser_IdIn(followingIds, pageable);

        // 3) Review -> FeedDto (널/빈 값 방어)
        return reviewPage.map(r -> {
            int likeCount = (int) likeRepository.countByReview(r);
            int commentCount = (int) commentRepository.countByReviewId(r.getId());

            // 사용자 정보 안전 추출
            var u = r.getUser();
            Long authorId = (u != null) ? u.getId() : null;
            String nickname = (u != null && u.getNickname() != null) ? u.getNickname() : "";
            String username = (u != null && u.getUsername() != null) ? u.getUsername() : nickname; // 폴백
            String tagId = (u != null) ? u.getTagId() : null;
            String profileImageUrl = (u != null && u.getProfileImageUrl() != null) ? u.getProfileImageUrl() : "";

            // 이미지/해시태그 안전 처리
            List<String> imageUrls = Optional.ofNullable(r.getReviewImageUrls()).orElseGet(List::of);
            List<String> hashtags = (r.getHashtags() != null)
                    ? r.getHashtags().stream()
                        .map(h -> h.getHashtag() != null ? h.getHashtag().getTagText() : null)
                        .filter(s -> s != null && !s.isBlank())
                        .collect(Collectors.toList())
                    : List.of();

            // 책 정보 안전 처리
            Long bookId = (r.getBook() != null) ? r.getBook().getId() : null;
            String bookTitle = (r.getBook() != null && r.getBook().getTitle() != null) ? r.getBook().getTitle() : null;

            // 별점 널 -> 0
            Integer rating = Optional.ofNullable(r.getRating()).orElse(0);

            return FeedDto.builder()
                    .reviewId(r.getId())
                    .userId(authorId)
                    .nickname(nickname)
                    .username(username)             // ✅ 추가 채움
                    .tagId(tagId)
                    .userProfileImage(profileImageUrl)
                    .content(Optional.ofNullable(r.getContent()).orElse("")) // 널 방어
                    .rating(rating)
                    .reviewImageUrls(imageUrls)
                    .createdAt(r.getCreatedAt())
                    .likeCount(likeCount)
                    .commentCount(commentCount)
                    .hashtags(hashtags)
                    .bookId(bookId)
                    .bookTitle(bookTitle)
                    .shareUrl(shareLinkService.reviewUrl(r.getId()))         // ✅ 공유 링크
                    .build();
        });
    }
}
