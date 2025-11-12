package com.example.project.repository;

import com.example.project.entity.GroupLike;
import com.example.project.entity.GroupPost;
import com.example.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupLikeRepository extends JpaRepository<GroupLike, Long> {
    Optional<GroupLike> findByPostAndUser(GroupPost post, User user);
    int countByPost(GroupPost post);
    boolean existsByPost_IdAndUser_Id(Long postId, Long userId); // ✅
    List<GroupLike> findByPostOrderByIdDesc(GroupPost post);     // ✅
    long countByPost_User_IdAndPost_PostType(Long userId, GroupPost.PostType postType);
}
