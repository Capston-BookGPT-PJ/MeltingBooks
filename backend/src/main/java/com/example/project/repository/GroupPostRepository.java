package com.example.project.repository;

import com.example.project.entity.GroupPost;
import com.example.project.entity.GroupPost.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupPostRepository extends JpaRepository<GroupPost, Long> {

    // (핀 기준 헤더) pinned=true + 타입별, 고정 우선 정렬
    @Query("""
        SELECT gp
        FROM GroupPost gp
        WHERE gp.group.id = :groupId
          AND gp.pinned = true
          AND gp.postType = :type
        ORDER BY CASE WHEN gp.pinnedAt IS NULL THEN 1 ELSE 0 END,
                 gp.pinnedAt DESC,
                 gp.createdAt DESC,
                 gp.id DESC
    """)
    List<GroupPost> findPinnedByType(@Param("groupId") Long groupId,
                                     @Param("type") PostType type);

    // (핀 무시 헤더) 타입 전체를 헤더에 노출하고 싶을 때 사용
    @Query("""
        SELECT gp
        FROM GroupPost gp
        WHERE gp.group.id = :groupId
          AND gp.postType = :type
        ORDER BY CASE WHEN gp.pinnedAt IS NULL THEN 1 ELSE 0 END,
                 gp.pinnedAt DESC,
                 gp.createdAt DESC,
                 gp.id DESC
    """)
    List<GroupPost> findHeaderByTypeAll(@Param("groupId") Long groupId,
                                        @Param("type") PostType type);

    // 일반 피드(타입들 최신순 페이징) — 서비스에서 헤더 타입은 제외해서 넘기세요.
    Page<GroupPost> findByGroup_IdAndPostTypeIn(Long groupId,
                                                List<PostType> types,
                                                Pageable pageable);

    long countByUser_IdAndPostType(Long userId, PostType postType);
}
