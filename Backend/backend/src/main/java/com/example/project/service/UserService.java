package com.example.project.service;

import com.example.project.dto.BasicUserDto;
import com.example.project.dto.UserDto;
import com.example.project.entity.GroupMember;
import com.example.project.entity.User;
import com.example.project.enums.SocialLoginType;
import com.example.project.exception.UserNotFoundException;
import com.example.project.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final ReadingLogRepository readingLogRepository;
    private final ReviewRepository reviewRepository;
    private final ReadingGoalRepository readingGoalRepository;
    private final GroupMemberRepository groupMemberRepository;

    // 소셜 로그인: 있으면 로그인, 없으면 등록
    @Transactional
    public User loginOrRegister(String email, String nickname, String username, SocialLoginType type) {
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .nickname(nickname)
                            .username(username)
                            .build();
                    newUser.getSocialProviders().add(type.name());
                    return userRepository.save(newUser);
                });

        if (!user.getSocialProviders().contains(type.name())) {
            user.getSocialProviders().add(type.name());
            userRepository.save(user);
        }
        return user;
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. ID=" + id));
    }

    @Transactional
    public User createUser(User user) {
        if (user.getTagId() != null && userRepository.findByTagId(user.getTagId()).isPresent()) {
            throw new RuntimeException("이미 사용 중인 tagId입니다.");
        }
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, User updatedUser) {
        User user = findById(id);

        if (updatedUser.getTagId() != null && !Objects.equals(updatedUser.getTagId(), user.getTagId())) {
            if (userRepository.findByTagId(updatedUser.getTagId()).isPresent()) {
                throw new RuntimeException("이미 사용 중인 tagId입니다.");
            }
        }

        user.setEmail(updatedUser.getEmail());
        user.setNickname(updatedUser.getNickname());
        user.setUsername(updatedUser.getUsername());
        user.setProfileImageUrl(updatedUser.getProfileImageUrl());
        user.setBio(updatedUser.getBio());
        user.setTagId(updatedUser.getTagId());
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = findById(id);
        followRepository.deleteByFollower(user);
        followRepository.deleteByFollowing(user);
        readingLogRepository.deleteByUser(user);
        reviewRepository.deleteByUser(user);
        readingGoalRepository.deleteByUser(user);
        userRepository.delete(user);
    }

    // ---- DTO 변환 헬퍼: 그룹 이름 3개 주입 ----
    @Transactional(readOnly = true)
    public UserDto toUserDtoWithGroups(User user) {
        List<String> groupNames = groupMemberRepository
                .findTop3ByUserIdAndJoinStatusOrderByJoinedAtDesc(
                        user.getId(), GroupMember.JoinStatus.ACCEPTED
                )
                .stream()
                .map(m -> m.getGroup().getName())
                .filter(n -> n != null && !n.isBlank())
                .distinct()
                .limit(3)
                .collect(Collectors.toList());

        return UserDto.from(user, groupNames);
    }

    // ✅ 닉네임 검색 (부분 일치, 대소문자 무시) — 경량 DTO 반환
    @Transactional(readOnly = true)
    public List<BasicUserDto> searchByNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) return List.of();
        return userRepository.findByNicknameContainingIgnoreCase(nickname).stream()
                .map(BasicUserDto::from)
                .collect(Collectors.toList());
    }

    // ✅ 인기 유저 Top 10 (팔로워 수 많은 순) — 경량 DTO 반환
    @Transactional(readOnly = true)
    public List<BasicUserDto> getPopularUsers() {
        return userRepository.findPopularUsers(PageRequest.of(0, 10))
                .getContent()
                .stream()
                .map(BasicUserDto::from)
                .collect(Collectors.toList());
    }
}
