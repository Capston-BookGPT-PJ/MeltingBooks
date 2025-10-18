package com.example.project.repository;

import com.example.project.entity.Review;
import com.example.project.entity.ReviewLike;
import com.example.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    long countByReview(Review review);
    boolean existsByUserAndReview(User user, Review review);
    Optional<ReviewLike> findByUserAndReview(User user, Review review);
}
