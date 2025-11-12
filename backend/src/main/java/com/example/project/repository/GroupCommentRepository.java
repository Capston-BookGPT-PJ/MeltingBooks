package com.example.project.repository;

import com.example.project.entity.GroupComment;
import com.example.project.entity.GroupPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface GroupCommentRepository extends JpaRepository<GroupComment, Long> {

    /** 단일 포스트 댓글 수 */
    int countByPost(GroupPost post);

    /** 댓글 목록(오래된 순) 페이징 */
    Page<GroupComment> findByPost_IdOrderByCreatedAtAsc(Long postId, Pageable pageable);

    /** 여러 포스트 댓글 수 배치 집계 (postId -> count) */
    interface CountPerPost {
        Long getPostId();
        long getCnt();
    }

    @Query("""
        select c.post.id as postId, count(c) as cnt
        from GroupComment c
        where c.post.id in :postIds
        group by c.post.id
    """)
    List<CountPerPost> countByPostIds(@Param("postIds") Collection<Long> postIds);

    /** 하위호환용(StatsRepositoryImpl 등에서 사용) */
    @Query("""
        select count(c) 
        from GroupComment c
        where c.user.id = :userId
          and c.post.postType = :postType
          and c.post.user.id <> :notUserId
    """)
    long countByUser_IdAndPost_PostTypeAndPost_User_IdNot(
            @Param("userId") Long userId,
            @Param("postType") GroupPost.PostType postType,
            @Param("notUserId") Long notUserId
    );
}
