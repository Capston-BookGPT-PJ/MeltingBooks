package com.example.project.repository;

import com.example.project.entity.CommentLike;
import com.example.project.entity.Comment;
import com.example.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    long countByComment(Comment comment);
    boolean existsByUserAndComment(User user, Comment comment);
    Optional<CommentLike> findByUserAndComment(User user, Comment comment);
}
