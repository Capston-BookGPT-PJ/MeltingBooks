package com.example.project.repository;

import com.example.project.entity.Review;
import com.example.project.entity.User;
import com.example.project.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Collection;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 특정 사용자가 작성한 모든 리뷰 (필요 시 유지)
    List<Review> findByUser(User user);

    // ✅ 사용자 최신 리뷰(기존 호출 유지) + user 즉시 로딩
    @EntityGraph(attributePaths = {"user"})
    List<Review> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    // ✅ 특정 책의 모든 리뷰(기존 호출 유지) + user 즉시 로딩
    @EntityGraph(attributePaths = {"user"})
    List<Review> findByBook(Book book);

    // ✅ 여러 사용자 최신 리뷰(기존 호출 유지) + user 즉시 로딩 (List 반환)
    @EntityGraph(attributePaths = {"user"})
    List<Review> findByUser_IdInOrderByCreatedAtDesc(List<Long> userIds, Pageable pageable);

    // ✅ FeedService 호환: 여러 사용자 리뷰 Page 반환 (정렬·페이징은 Pageable로)
    @EntityGraph(attributePaths = {"user"})
    Page<Review> findByUser_IdIn(Collection<Long> userIds, Pageable pageable);

    // 특정 사용자의 모든 리뷰 삭제
    void deleteByUser(User user);

    /* =========================
       기간 집계 — [start, end) (끝 미포함) 규약
       ========================= */

    // 개인(userId): [start, end) — ✅ 개인 goal 조회용
    @Query("""
        select count(r)
        from Review r
        where r.user.id = :userId
          and r.createdAt >= :start
          and r.createdAt <  :end
    """)
    long countByUserIdAndCreatedAtRange(@Param("userId") Long userId,
                                        @Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end);

    // 그룹(여러 사용자): [start, end) — ✅ 그룹 집계용
    @Query("""
        select count(r)
        from Review r
        where r.user.id in :userIds
          and r.createdAt >= :start
          and r.createdAt <  :end
    """)
    long countByUsersAndCreatedAtRange(@Param("userIds") Collection<Long> userIds,
                                       @Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);

    /* =========================
       해시태그 검색 (유지)
       ========================= */
    @EntityGraph(attributePaths = {"user"})
    @Query("""
           select r
           from Review r
             join r.hashtags rh
             join rh.hashtag h
           where h.tagText = :tagText
           order by r.createdAt desc
           """)
    List<Review> findByHashtagOrderByCreatedAtDesc(@Param("tagText") String tagText, Pageable pageable);
}
