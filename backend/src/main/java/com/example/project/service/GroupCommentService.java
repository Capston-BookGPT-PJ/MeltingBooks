package com.example.project.service;

import com.example.project.dto.GroupCommentCreateRequest;
import com.example.project.dto.GroupCommentDto;
import com.example.project.dto.GroupCommentUpdateRequest;
import com.example.project.entity.Group;
import com.example.project.entity.GroupComment;
import com.example.project.entity.GroupMember;
import com.example.project.entity.GroupPost;
import com.example.project.entity.User;
import com.example.project.repository.GroupCommentRepository;
import com.example.project.repository.GroupMemberRepository;
import com.example.project.repository.GroupPostRepository;
import com.example.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// ✅ 추가: 배지 자동 평가용
import com.example.project.service.badge.BadgeService;

@Service
@RequiredArgsConstructor
public class GroupCommentService {

    private final GroupPostRepository groupPostRepository;
    private final GroupCommentRepository groupCommentRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    // ✅ 추가
    private final BadgeService badgeService;

    private void assertAcceptedMember(Group group, User user) {
        GroupMember m = groupMemberRepository.findByGroupAndUser(group, user)
                .orElseThrow(() -> new IllegalStateException("그룹 멤버만 이용할 수 있습니다."));
        if (m.getJoinStatus() != GroupMember.JoinStatus.ACCEPTED) {
            throw new IllegalStateException("그룹 가입이 승인되지 않았습니다.");
        }
    }

    private void assertReviewType(GroupPost post) {
        if (post.getPostType() != GroupPost.PostType.REVIEW) {
            throw new IllegalStateException("댓글은 일반글(REVIEW)에만 작성할 수 있습니다.");
        }
    }

    private GroupPost getPostOrThrow(Long groupId, Long postId) {
        GroupPost post = groupPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        if (!post.getGroup().getId().equals(groupId)) {
            throw new IllegalArgumentException("그룹이 일치하지 않습니다.");
        }
        return post;
    }

    @Transactional
    public GroupCommentDto add(Long groupId, Long postId, Long userId, GroupCommentCreateRequest req) {
        GroupPost post = getPostOrThrow(groupId, postId);
        assertReviewType(post);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 승인 멤버만 댓글 허용
        assertAcceptedMember(post.getGroup(), user);

        if (req.getContent() == null || req.getContent().isBlank()) {
            throw new IllegalArgumentException("댓글 내용을 입력해주세요.");
        }

        GroupComment comment = GroupComment.builder()
                .post(post)
                .user(user)
                .content(req.getContent().trim())
                .build();

        GroupComment saved = groupCommentRepository.save(comment);

        // ✅ 배지 자동 평가 (댓글 작성자 기준)
        // ReviewShareMaster(소통) 쪽 누적/연속 조건을 즉시 반영
        badgeService.evaluateAll(userId);

        return GroupCommentDto.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<GroupCommentDto> list(Long groupId, Long postId, Pageable pageable) {
        GroupPost post = getPostOrThrow(groupId, postId);
        assertReviewType(post);
        return groupCommentRepository
                .findByPost_IdOrderByCreatedAtAsc(post.getId(), pageable)
                .map(GroupCommentDto::from);
    }

    @Transactional
    public GroupCommentDto update(Long groupId, Long postId, Long commentId, Long userId, GroupCommentUpdateRequest req) {
        GroupPost post = getPostOrThrow(groupId, postId);
        assertReviewType(post);

        GroupComment comment = groupCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        if (!comment.getPost().getId().equals(post.getId())) {
            throw new IllegalArgumentException("댓글이 해당 게시글에 속하지 않습니다.");
        }
        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalStateException("댓글 수정 권한이 없습니다.");
        }
        if (req.getContent() == null || req.getContent().isBlank()) {
            throw new IllegalArgumentException("댓글 내용을 입력해주세요.");
        }

        comment.setContent(req.getContent().trim());
        GroupComment saved = groupCommentRepository.save(comment);

        // (선택) 내용수정은 카운트 변화 없음 → 보통 재평가 불필요
        return GroupCommentDto.from(saved);
    }

    @Transactional
    public void delete(Long groupId, Long postId, Long commentId, Long userId) {
        GroupPost post = getPostOrThrow(groupId, postId);
        assertReviewType(post);

        GroupComment comment = groupCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        if (!comment.getPost().getId().equals(post.getId())) {
            throw new IllegalArgumentException("댓글이 해당 게시글에 속하지 않습니다.");
        }

        boolean isAuthor = comment.getUser().getId().equals(userId);
        boolean isOwner  = post.getGroup().getOwner() != null
                && post.getGroup().getOwner().getId().equals(userId);

        if (!isAuthor && !isOwner) {
            throw new IllegalStateException("댓글 삭제 권한이 없습니다.");
        }
        groupCommentRepository.delete(comment);

        // (선택) 댓글 삭제 시에도 즉시 재평가하고 싶으면 활성화
        // badgeService.evaluateAll(userId);
    }
}
