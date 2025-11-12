package com.example.project.repository;

import com.example.project.entity.Review;
import com.example.project.entity.ReviewHashtag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewHashtagRepository extends JpaRepository<ReviewHashtag, Long> {

    List<ReviewHashtag> findByReview(com.example.project.entity.Review review);
    void deleteByReview(com.example.project.entity.Review review);

    @Query("""
           SELECT r
           FROM ReviewHashtag rh
             JOIN rh.review r
             JOIN rh.hashtag h
           WHERE h.tagText = :tagText
           ORDER BY r.createdAt DESC
           """)
    Page<Review> findReviewsByTagOrderByCreatedAtDesc(@Param("tagText") String tagText, Pageable pageable);

    @Query("""
           SELECT h.tagText AS tag, COUNT(rh.review.id) AS cnt
           FROM ReviewHashtag rh
             JOIN rh.hashtag h
           GROUP BY h.tagText
           ORDER BY cnt DESC
           """)
    Page<PopularHashtagProjection> findPopularTags(Pageable pageable);

    interface PopularHashtagProjection {
        String getTag();
        Long getCnt();
    }
}
