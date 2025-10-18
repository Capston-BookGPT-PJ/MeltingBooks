package com.example.project.repository;

import com.example.project.dto.LikerDto;
import com.example.project.entity.Comment;
import com.example.project.entity.Like;
import com.example.project.entity.Review;
import com.example.project.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    boolean existsByUserAndReview(User user, Review review);
    boolean existsByUserAndComment(User user, Comment comment);

    Optional<Like> findByUserAndReview(User user, Review review);
    Optional<Like> findByUserAndComment(User user, Comment comment);

    void deleteByUserAndReview(User user, Review review);
    void deleteByUserAndComment(User user, Comment comment);

    long countByReview(Review review);
    long countByComment(Comment comment);

    // likedByMe 계산용
    boolean existsByUser_IdAndReview_Id(Long userId, Long reviewId);
    boolean existsByUser_IdAndComment_CommentId(Long userId, Long commentId);

    // ✅ 최근 좋아요 유저 (리뷰) — DTO 매핑 (FQN 사용, 텍스트블록 제거)
    @Query("select new com.example.project.dto.LikerDto(u.id, u.nickname, u.profileImageUrl) " +
           "from com.example.project.entity.Like l " +
           "join l.user u " +
           "where l.review.id = :reviewId " +
           "order by l.id desc")
    List<LikerDto> findRecentLikersOfReview(@Param("reviewId") Long reviewId, Pageable pageable);

    // ✅ 최근 좋아요 유저 (댓글) — DTO 매핑 (FQN 사용)
    @Query("select new com.example.project.dto.LikerDto(u.id, u.nickname, u.profileImageUrl) " +
           "from com.example.project.entity.Like l " +
           "join l.user u " +
           "where l.comment.commentId = :commentId " +
           "order by l.id desc")
    List<LikerDto> findRecentLikersOfComment(@Param("commentId") Long commentId, Pageable pageable);

    // 참고: 엔티티 자체가 필요할 때
    List<Like> findByReview(Review review);
    List<Like> findByComment(Comment comment);
}
