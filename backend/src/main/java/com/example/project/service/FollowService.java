package com.example.project.service;

import com.example.project.dto.FollowDto;
import com.example.project.entity.Follow;
import com.example.project.entity.User;
import com.example.project.repository.FollowRepository;
import com.example.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    // 팔로우
    @Transactional
    public void follow(Long followerId, Long followingId) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new RuntimeException("Follower not found"));
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new RuntimeException("Following not found"));

        if (followerId.equals(followingId)) {
            throw new RuntimeException("자기 자신은 팔로우할 수 없습니다.");
        }

        boolean exists = followRepository.findByFollower(follower).stream()
                .anyMatch(f -> f.getFollowing().getId().equals(followingId));

        if (!exists) {
            Follow follow = Follow.builder()
                    .follower(follower)
                    .following(following)
                    .build();
            followRepository.save(follow);
        }
    }

    // 언팔로우
    @Transactional
    public void unfollow(Long followerId, Long followingId) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new RuntimeException("Follower not found"));
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new RuntimeException("Following not found"));

        followRepository.deleteByFollowerAndFollowing(follower, following);
    }

    // 팔로워 조회 (나를 팔로우한 사람들)
    public List<FollowDto> followers(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return followRepository.findByFollowing(user).stream()
                .map(f -> FollowDto.from(f.getFollower()))
                .toList();
    }

    // 팔로잉 조회 (내가 팔로우한 사람들)
    public List<FollowDto> following(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return followRepository.findByFollower(user).stream()
                .map(f -> FollowDto.from(f.getFollowing()))
                .toList();
    }
}