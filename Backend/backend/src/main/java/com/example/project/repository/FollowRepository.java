package com.example.project.repository;

import com.example.project.entity.Follow;
import com.example.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    // 관계 존재 여부
    boolean existsByFollowerAndFollowing(User follower, User following);

    // 특정 관계 1건 삭제
    void deleteByFollowerAndFollowing(User follower, User following);

    // 조회
    List<Follow> findByFollower(User follower);
    List<Follow> findByFollowing(User following);

    // 특정 사용자가 팔로우한 모든 관계 삭제
    void deleteByFollower(User follower);

    // 특정 사용자를 팔로우하는 모든 관계 삭제
    void deleteByFollowing(User following);

    // ID 기반 삭제 (UserService에서 사용 가능)
    @Transactional
    void deleteByFollowerId(Long followerId);

    @Transactional
    void deleteByFollowingId(Long followingId);

    // 팔로잉 ID 리스트 조회
    @Query("SELECT f.following.id FROM Follow f WHERE f.follower.id = :followerId")
    List<Long> findFollowingIdsByFollowerId(@Param("followerId") Long followerId);
}
