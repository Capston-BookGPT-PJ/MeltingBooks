package com.example.project.service;

import com.example.project.dto.CommentDto;
import com.example.project.dto.LikerDto;
import com.example.project.entity.Comment;
import com.example.project.entity.Review;
import com.example.project.entity.User;
import com.example.project.repository.CommentRepository;
import com.example.project.repository.LikeRepository;
import com.example.project.repository.ReviewRepository;
import com.example.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final LikeRepository likeRepository;

    // 댓글 생성
    @Transactional
    public CommentDto create(Long userId, Long reviewId, String content) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));

        Comment comment = Comment.builder()
                .content(content)
                .user(user)
                .review(review)
                .build();

        Comment saved = commentRepository.save(comment);
        // 생성 직후 likedByMe=false, likedUsers=[]
        return toDto(saved, userId);
    }

    // 댓글 수정
    @Transactional
    public CommentDto update(Long commentId, Long userId, String content) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));
        if (!comment.getUser().getId().equals(userId)) {
            throw new RuntimeException("수정 권한이 없습니다.");
        }

        comment.setContent(content);
        return toDto(comment, userId);
    }

    // 댓글 삭제
    @Transactional
    public void delete(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));
        if (!comment.getUser().getId().equals(userId)) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }
        commentRepository.delete(comment);
    }

    // 특정 리뷰의 댓글 조회 (현재 로그인 사용자 기준 likedByMe 포함)
    @Transactional(readOnly = true)
    public List<CommentDto> getByReview(Long reviewId, Long currentUserId) {
        return commentRepository.findByReviewId(reviewId)
                .stream()
                .map(c -> toDto(c, currentUserId))
                .collect(Collectors.toList());
    }

    // (기존 시그니처 유지용 — likedByMe는 항상 false가 될 수 있음)
    @Transactional(readOnly = true)
    public List<CommentDto> getByReview(Long reviewId) {
        return getByReview(reviewId, null);
    }

    // ===== 매핑 헬퍼 =====
    private CommentDto toDto(Comment c, Long currentUserId) {
        int likeCount = (int) likeRepository.countByComment(c);
        int replyCount = 0; // 대댓글 사용 시 repository로 교체

        // 내가 좋아요 눌렀는지
        boolean likedByMe = false;
        if (currentUserId != null) {
            // 성능을 위해 User 엔티티 전체 조회 대신 ID 비교용 existsBy... 쓰려면 User가 필요
            // → findById 사용 (대부분 캐시/영속성 컨텍스트에 있을 가능성 높음)
            User me = userRepository.findById(currentUserId).orElse(null);
            if (me != null) {
                likedByMe = likeRepository.existsByUserAndComment(me, c);
            }
        }

        // 좋아요 누른 유저 목록(최신순)
        var likedUsers = likeRepository.findByComment(c).stream()
        .map(like -> {
            var u = like.getUser();
            return LikerDto.builder()
                    .userId(u != null ? u.getId() : null)
                    .nickname(u != null ? u.getNickname() : null)
                    .profileImageUrl(u != null ? u.getProfileImageUrl() : null)
                    .build();
        })
        .collect(Collectors.toList());



        return CommentDto.builder()
                .commentId(c.getId())
                .userId(c.getUser() != null ? c.getUser().getId() : null)
                .nickname(c.getUser() != null ? c.getUser().getNickname() : null)
                .tagId(c.getUser() != null ? c.getUser().getTagId() : null)
                .userProfileImage(c.getUser() != null ? c.getUser().getProfileImageUrl() : null)
                .content(c.getContent())
                .createdAt(c.getCreatedAt())
                .likeCount(likeCount)
                .replyCount(replyCount)
                .likedByMe(likedByMe)
                .likedUsers(likedUsers)
                .build();
    }
}
