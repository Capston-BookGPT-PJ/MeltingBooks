package com.example.project.repository;

import com.example.project.entity.Comment;
import com.example.project.entity.Review;
import com.example.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 특정 리뷰의 댓글 전체
    List<Comment> findByReviewId(Long reviewId);

    // 특정 유저가 쓴 댓글 전체 (필요하면)
    List<Comment> findByUser(User user);

    // ✅ 댓글 개수 카운트 (FeedService에서 사용)
    long countByReviewId(Long reviewId);
}
