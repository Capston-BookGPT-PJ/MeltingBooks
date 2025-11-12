package com.example.project.service;

import com.example.project.dto.GroupDto;
import com.example.project.dto.FeedDto;
import com.example.project.repository.GroupCommentRepository;
import com.example.project.dto.GroupFeedSectionResponse;
import com.example.project.dto.GroupMemberDto;
import com.example.project.dto.MyGroupDto;
import com.example.project.dto.request.GroupCreateRequest;
import com.example.project.dto.LikerDto;
import com.example.project.entity.Group;
import com.example.project.entity.GroupMember;
import com.example.project.entity.GroupPost;
import com.example.project.entity.User;
import com.example.project.repository.GroupMemberRepository;
import com.example.project.repository.GroupPostRepository;
import com.example.project.repository.GroupRepository;
import com.example.project.repository.UserRepository;
import com.example.project.repository.GroupLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.example.project.dto.GroupGoalCardDto;
import com.example.project.repository.GroupGoalRepository;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class GroupService {

    private static final int GROUP_LIMIT = 3;

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final GroupPostRepository groupPostRepository;
    private final GroupLikeRepository groupLikeRepository;
    private final GroupCommentRepository groupCommentRepository;
    private final GroupGoalRepository groupGoalRepository;


    /* =========================
       공통 헬퍼
       ========================= */
    private int acceptedCount(Long userId) {
        return groupMemberRepository.countByUserIdAndJoinStatus(userId, GroupMember.JoinStatus.ACCEPTED);
    }

    private void ensureUnderLimit(Long userId, int toAdd) {
        int total = acceptedCount(userId);
        if (total + toAdd > GROUP_LIMIT) {
            throw new IllegalStateException("그룹은 만든 그룹과 가입한 그룹을 합쳐 최대 " + GROUP_LIMIT + "개까지 가능합니다.");
        }
    }

    /* =========================
       그룹 생성
       ========================= */
    @Transactional
    public GroupDto createGroup(GroupCreateRequest req, Long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        ensureUnderLimit(userId, 1);

        Group group = Group.builder()
                .name(req.getName())
                .description(req.getDescription())
                .groupImageUrl(req.getGroupImageUrl())
                .category(req.getCategory())
                .owner(owner)
                .build();

        Group savedGroup = groupRepository.save(group);

        GroupMember ownerMember = GroupMember.builder()
                .group(savedGroup)
                .user(owner)
                .joinStatus(GroupMember.JoinStatus.ACCEPTED)
                .joinedAt(LocalDateTime.now())
                .build();
        groupMemberRepository.save(ownerMember);

        return GroupDto.fromEntity(savedGroup, 1);
    }

    /* =========================
       그룹 수정
       ========================= */
    @Transactional
    public GroupDto updateGroup(Long groupId, Long userId, GroupCreateRequest req) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        if (!group.getOwner().getId().equals(userId)) {
            throw new IllegalStateException("그룹장만 그룹 정보를 수정할 수 있습니다.");
        }

        group.setName(req.getName());
        group.setDescription(req.getDescription());
        group.setGroupImageUrl(req.getGroupImageUrl());
        group.setCategory(req.getCategory());

        int acceptedCount = groupMemberRepository.countByGroupIdAndJoinStatus(groupId, GroupMember.JoinStatus.ACCEPTED);
        return GroupDto.fromEntity(groupRepository.save(group), acceptedCount);
    }

    /* =========================
       그룹 삭제
       ========================= */
    @Transactional
    public void deleteGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        if (!group.getOwner().getId().equals(userId)) {
            throw new IllegalStateException("그룹장만 그룹을 삭제할 수 있습니다.");
        }

        groupRepository.delete(group);
    }

    /* =========================
       그룹 가입 요청
       ========================= */
    @Transactional
    public void requestToJoin(Long groupId, Long userId) {
        ensureUnderLimit(userId, 1);

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        groupMemberRepository.findByGroupAndUser(group, user)
                .ifPresent(m -> { throw new IllegalStateException("이미 가입했거나 가입 대기 중입니다."); });

        GroupMember newMember = GroupMember.builder()
                .group(group)
                .user(user)
                .joinStatus(GroupMember.JoinStatus.PENDING)
                .joinedAt(null)
                .build();
        groupMemberRepository.save(newMember);
    }

    /* =========================
       가입 요청 승인(그룹장 전용)
       ========================= */
    @Transactional
    public void acceptJoinRequest(Long groupId, Long ownerId, Long memberUserId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        if (!group.getOwner().getId().equals(ownerId)) {
            throw new IllegalStateException("권한이 없습니다. (그룹장만 승인할 수 있음)");
        }

        User target = userRepository.findById(memberUserId)
                .orElseThrow(() -> new IllegalArgumentException("대상 사용자를 찾을 수 없습니다."));

        GroupMember member = groupMemberRepository.findByGroupAndUser(group, target)
                .orElseThrow(() -> new IllegalStateException("가입 요청을 찾을 수 없습니다."));

        if (member.getJoinStatus() != GroupMember.JoinStatus.PENDING) {
            throw new IllegalStateException("대기(PENDING) 상태가 아니어서 승인할 수 없습니다.");
        }

        ensureUnderLimit(memberUserId, 1);

        member.setJoinStatus(GroupMember.JoinStatus.ACCEPTED);
        if (member.getJoinedAt() == null) {
            member.setJoinedAt(LocalDateTime.now());
        }
        groupMemberRepository.save(member);
    }

    /* =========================
       가입 요청 목록 조회(그룹장 전용)
       ========================= */
    @Transactional(readOnly = true)
    public Page<GroupMemberDto> getJoinRequests(Long groupId, Long ownerId, Pageable pageable) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        if (!group.getOwner().getId().equals(ownerId)) {
            throw new IllegalStateException("그룹장만 가입 요청을 조회할 수 있습니다.");
        }

        return groupMemberRepository
                .findByGroupIdAndJoinStatus(groupId, GroupMember.JoinStatus.PENDING, pageable)
                .map(GroupMemberDto::from);
    }

    /* =========================
       가입 요청 거절(그룹장 전용)
       ========================= */
    @Transactional
    public void rejectJoinRequest(Long groupId, Long ownerId, Long memberUserId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        if (!group.getOwner().getId().equals(ownerId)) {
            throw new IllegalStateException("그룹장만 가입 요청을 거절할 수 있습니다.");
        }

        User target = userRepository.findById(memberUserId)
                .orElseThrow(() -> new IllegalArgumentException("대상 사용자를 찾을 수 없습니다."));

        GroupMember member = groupMemberRepository.findByGroupAndUser(group, target)
                .orElseThrow(() -> new IllegalStateException("가입 요청을 찾을 수 없습니다."));

        if (member.getJoinStatus() != GroupMember.JoinStatus.PENDING) {
            throw new IllegalStateException("대기(PENDING) 상태가 아니어서 거절할 수 없습니다.");
        }

        // 정책 1) 행 삭제
        groupMemberRepository.delete(member);

        // 정책 2) 만약 상태값을 유지하고 싶다면 아래로 대체:
        // member.setJoinStatus(GroupMember.JoinStatus.REJECTED);
        // member.setJoinedAt(null);
        // groupMemberRepository.save(member);
    }

    /* =========================
       그룹장 위임
       ========================= */
    @Transactional
    public void delegateOwner(Long groupId, Long currentOwnerId, Long newOwnerId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        if (!group.getOwner().getId().equals(currentOwnerId)) {
            throw new IllegalStateException("그룹장만 권한을 위임할 수 있습니다.");
        }

        User newOwner = userRepository.findById(newOwnerId)
                .orElseThrow(() -> new IllegalArgumentException("새로운 그룹장을 찾을 수 없습니다."));

        groupMemberRepository.findByGroupAndUser(group, newOwner)
                .orElseThrow(() -> new IllegalStateException("새로운 그룹장은 현재 그룹의 멤버여야 합니다."));

        group.setOwner(newOwner);
        groupRepository.save(group);
    }

    /* =========================
       그룹 탈퇴
       ========================= */
    @Transactional
    public void leaveGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (group.getOwner().getId().equals(userId)) {
            throw new IllegalStateException("그룹장은 그룹을 탈퇴할 수 없습니다. 그룹 삭제 또는 권한 위임 후 탈퇴하세요.");
        }

        GroupMember member = groupMemberRepository.findByGroupAndUser(group, user)
                .orElseThrow(() -> new IllegalStateException("그룹 멤버가 아닙니다."));

        groupMemberRepository.delete(member);
    }

    /* =========================
       그룹 검색
       ========================= */
    @Transactional(readOnly = true)
    public List<GroupDto> searchGroups(String keyword, String category) {
        List<Group> groups;
        if (keyword != null && !keyword.isBlank()) {
            groups = groupRepository.findByNameContainingIgnoreCase(keyword);
        } else if (category != null && !category.isBlank()) {
            groups = groupRepository.findByCategory(category);
        } else {
            groups = groupRepository.findAll();
        }

        return groups.stream()
                .map(g -> GroupDto.fromEntity(
                        g,
                        groupMemberRepository.countByGroupIdAndJoinStatus(g.getId(), GroupMember.JoinStatus.ACCEPTED)
                ))
                .collect(Collectors.toList());
    }

    /* =========================
       그룹 프로필 조회 (멤버 목록 포함, ACCEPTED 기준)
       ========================= */
    @Transactional(readOnly = true)
    public GroupDto getGroupProfile(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        Page<GroupMember> page = groupMemberRepository.findByGroupIdAndJoinStatus(
                groupId, GroupMember.JoinStatus.ACCEPTED, Pageable.unpaged()
        );

        List<GroupMemberDto> members = page.getContent().stream()
                .map(GroupMemberDto::from)
                .collect(Collectors.toList());

        int memberCount = members.size();

        return GroupDto.fromEntity(group, memberCount, members);
    }

    /* =========================
       그룹 멤버 조회
       ========================= */
    @Transactional(readOnly = true)
    public Page<GroupMemberDto> getGroupMembers(Long groupId, Pageable pageable) {
        return groupMemberRepository
                .findByGroupIdAndJoinStatus(groupId, GroupMember.JoinStatus.ACCEPTED, pageable)
                .map(GroupMemberDto::from);
    }

    /* =========================
       내 그룹 조회
       ========================= */
    @Transactional(readOnly = true)
    public List<MyGroupDto> getMyGroups(Long userId) {
        return groupMemberRepository
                .findByUserIdAndJoinStatus(userId, GroupMember.JoinStatus.ACCEPTED)
                .stream()
                .map(m -> MyGroupDto.builder()
                        .groupId(m.getGroup().getId())
                        .name(m.getGroup().getName())
                        .description(m.getGroup().getDescription())
                        .category(m.getGroup().getCategory())
                        .memberCount(
                                groupMemberRepository.countByGroupIdAndJoinStatus(
                                        m.getGroup().getId(), GroupMember.JoinStatus.ACCEPTED
                                )
                        )
                        .build())
                .collect(Collectors.toList());
    }

    /* =========================
       그룹 피드 조회
       ========================= */
    @Transactional(readOnly = true)
public GroupFeedSectionResponse getGroupFeed(Long groupId,
                                             Long currentUserId,
                                             List<GroupPost.PostType> types,
                                             Pageable pageable) {

    List<FeedDto> notices = groupPostRepository
            .findHeaderByTypeAll(groupId, GroupPost.PostType.NOTICE)
            .stream()
            .map(p -> toFeedDto(p, currentUserId))
            .toList();

    List<FeedDto> recommendedBooks = groupPostRepository
            .findHeaderByTypeAll(groupId, GroupPost.PostType.RECOMMENDED_BOOK)
            .stream()
            .map(p -> toFeedDto(p, currentUserId))
            .toList();

    // ✅ goals: GroupGoal 기반으로 교체 (오늘 기준 활성 목표)
    var goals = groupGoalRepository
            .findActiveByGroupOnDate(groupId, LocalDate.now())
            .stream()
            .map(GroupGoalCardDto::fromEntity)
            .toList();

    // 일반 피드쪽은 그대로 (리뷰형만)
    List<GroupPost.PostType> headerTypes = List.of(
            GroupPost.PostType.NOTICE,
            GroupPost.PostType.RECOMMENDED_BOOK,
            GroupPost.PostType.GOAL_SHARE   // 텍스트형 목표는 헤더용이지만, 이제 화면에 안 씀
    );

    List<GroupPost.PostType> effectiveTypes =
            (types == null || types.isEmpty())
                    ? List.of(GroupPost.PostType.REVIEW)
                    : types.stream().filter(t -> !headerTypes.contains(t)).toList();

    if (effectiveTypes.isEmpty()) {
        effectiveTypes = List.of(GroupPost.PostType.REVIEW);
    }

    Page<GroupPost> postsPage = groupPostRepository
            .findByGroup_IdAndPostTypeIn(groupId, effectiveTypes, pageable);

    Page<FeedDto> posts = postsPage.map(p -> toFeedDto(p, currentUserId));

    return GroupFeedSectionResponse.builder()
            .notices(notices)
            .recommendedBooks(recommendedBooks)
            .goals(goals)     // ✅ 변경된 타입
            .posts(posts)
            .build();
}

    private FeedDto toFeedDto(GroupPost post, Long currentUserId) {
        boolean interactive = post.getPostType() == GroupPost.PostType.REVIEW;

        int likeCount = 0;
        boolean likedByMe = false;
        List<LikerDto> likedUsers = Collections.emptyList();
        int commentCount = 0;

        if (interactive) {
            likeCount = groupLikeRepository.countByPost(post);
            likedByMe = currentUserId != null
                    && groupLikeRepository.existsByPost_IdAndUser_Id(post.getId(), currentUserId);
            likedUsers = groupLikeRepository.findByPostOrderByIdDesc(post).stream()
                    .map(gl -> LikerDto.builder()
                            .userId(gl.getUser().getId())
                            .nickname(gl.getUser().getNickname())
                            .profileImageUrl(gl.getUser().getProfileImageUrl())
                            .build())
                    .toList();
            commentCount = groupCommentRepository.countByPost(post);
        }

        return FeedDto.builder()
                .reviewId(post.getId())
                .userId(post.getUser().getId())
                .tagId(null)
                .nickname(post.getUser().getNickname())
                .username(post.getUser().getUsername())
                .userProfileImage(post.getUser().getProfileImageUrl())
                .content(post.getContent())
                .rating(0)
                .reviewImageUrls(post.getImageUrlList())
                .createdAt(post.getCreatedAt())
                .likeCount(likeCount)
                .commentCount(commentCount)
                .hashtags(List.of())
                .likedByMe(likedByMe)
                .likedUsers(likedUsers)
                .bookId(null)
                .bookTitle(null)
                .shareUrl(null)
                .build();
    }
}
