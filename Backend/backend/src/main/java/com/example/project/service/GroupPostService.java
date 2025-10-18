package com.example.project.service;

import com.example.project.dto.GroupPostDto;
import com.example.project.dto.GroupPostCreateRequest;
import com.example.project.dto.GroupPostUpdateRequest;
import com.example.project.dto.LikerDto;
import com.example.project.entity.*;
import com.example.project.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;
import java.util.Objects;

// ✅ 배지
import com.example.project.service.badge.BadgeService;
import com.example.project.enums.BadgeType;

@Service
@RequiredArgsConstructor
public class GroupPostService {

    private final GroupPostRepository groupPostRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupLikeRepository groupLikeRepository;
    private final GroupCommentRepository groupCommentRepository;
    private final GroupPostImageRepository groupPostImageRepository;
    private final StorageService storageService;

    // ✅ 배지 서비스 주입
    private final BadgeService badgeService;

    /* -----------------------------------------
       권한/유틸
       ----------------------------------------- */

    /** 해당 그룹의 '승인(ACCEPTED)' 멤버 여부 확인 */
    private void assertAcceptedMember(Group group, User user) {
        GroupMember member = groupMemberRepository.findByGroupAndUser(group, user)
                .orElseThrow(() -> new IllegalStateException("그룹 멤버만 이용할 수 있습니다."));
        if (member.getJoinStatus() != GroupMember.JoinStatus.ACCEPTED) {
            throw new IllegalStateException("그룹 가입이 승인되지 않았습니다.");
        }
    }

    private boolean isOwner(Group group, Long userId) {
        return group.getOwner() != null && group.getOwner().getId().equals(userId);
    }

    /** 공지/공동목표/추천도서 → 그룹장 전용 타입 */
    private void requireOwnerForOwnerOnlyTypes(GroupPost.PostType type, Group group, Long userId) {
        if (type == GroupPost.PostType.NOTICE
                || type == GroupPost.PostType.GOAL_SHARE
                || type == GroupPost.PostType.RECOMMENDED_BOOK) {
            if (!isOwner(group, userId)) {
                throw new IllegalStateException("공지/공통 목표/추천 도서는 그룹장만 작성/수정/삭제할 수 있습니다.");
            }
        }
    }

    private void assertTypeAllowed(GroupPost.PostType type) {
        switch (type) {
            case NOTICE, REVIEW, GOAL_SHARE, RECOMMENDED_BOOK -> { /* ok */ }
            default -> throw new IllegalStateException("허용되지 않은 게시글 타입입니다.");
        }
    }

    /** ✅ 좋아요/댓글 허용 타입(=REVIEW)만 인터랙션 가능 */
    private boolean isInteractive(GroupPost.PostType type) {
        return type == GroupPost.PostType.REVIEW;
    }

    private List<String> mergeImageUrls(List<String> base, List<String> extra) {
        List<String> out = new ArrayList<>();
        if (base != null) out.addAll(base);
        if (extra != null) out.addAll(extra);
        return out.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    /** post.images 전체 교체 */
    private void replaceImages(GroupPost post, List<String> urls) {
        post.getImages().clear();
        if (urls == null || urls.isEmpty()) return;

        int order = 0;
        for (String u : urls) {
            GroupPostImage img = GroupPostImage.builder()
                    .post(post)
                    .imageUrl(u)
                    .sortOrder(order++)
                    .build();
            post.getImages().add(img);
        }
    }

    /* --------- 파일명/확장자 유틸 --------- */

    private String guessExt(MultipartFile f) {
        String ext = null;
        String original = f.getOriginalFilename();
        if (original != null) {
            int dot = original.lastIndexOf('.');
            if (dot >= 0 && dot < original.length() - 1) {
                ext = original.substring(dot).toLowerCase(); // ".jpg"
            }
        }
        if (ext == null || ext.isBlank()) {
            String ct = f.getContentType();
            if (ct != null) {
                switch (ct) {
                    case "image/jpeg" -> ext = ".jpg";
                    case "image/png" -> ext = ".png";
                    case "image/webp" -> ext = ".webp";
                    case "image/gif" -> ext = ".gif";
                    default -> ext = "";
                }
            } else {
                ext = "";
            }
        }
        return ext;
    }

    private String buildGroupPostObjectName(Long groupId, Long postId, MultipartFile f) {
        String ext = guessExt(f);
        return "group-post/" + groupId + "_" + postId + "_" + UUID.randomUUID() + ext;
    }

    /* -----------------------------------------
       생성
       ----------------------------------------- */
    @Transactional
    public GroupPostDto createPostJson(Long groupId, Long userId, GroupPostCreateRequest req) {

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 타입 검증 + 권한
        assertTypeAllowed(req.getPostType());
        if (req.getPostType() == GroupPost.PostType.REVIEW) {
            assertAcceptedMember(group, user);
        } else {
            requireOwnerForOwnerOnlyTypes(req.getPostType(), group, userId);
        }

        GroupPost post = GroupPost.builder()
                .group(group)
                .user(user)
                .postType(req.getPostType())
                .title(req.getTitle())
                .content(req.getContent())
                .build();

        // 외부 URL을 JSON으로 받는 경우만 반영(선택)
        if (req.getImageUrls() != null && !req.getImageUrls().isEmpty()) {
            replaceImagesWithUrls(post, req.getImageUrls());
        }

        GroupPost saved = groupPostRepository.save(post);

        // ✅ 리뷰 글 작성 → 배지 자동 갱신 (공유/소통 관련)
        if (saved.getPostType() == GroupPost.PostType.REVIEW) {
            // 넓게 모든 배지 재평가(리뷰/소통 규칙 포함)
            badgeService.evaluateAll(userId);
            // 필요 시 특정 타입만:
            // badgeService.evaluateAndAward(BadgeType.REVIEW_SHARE_MASTER, userId);
        }

        return GroupPostDto.fromEntity(saved);
    }

    /* ========================
       글 수정(JSON, raw)
       ======================== */
    @Transactional
    public GroupPostDto updatePostJson(Long postId, Long userId, GroupPostUpdateRequest req) {

        GroupPost post = groupPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        Group group = post.getGroup();

        // 타입 변경 허용 시 검증
        if (req.getPostType() != null && req.getPostType() != post.getPostType()) {
            assertTypeAllowed(req.getPostType());
            if (req.getPostType() == GroupPost.PostType.REVIEW) {
                assertAcceptedMember(group, post.getUser());
            } else {
                requireOwnerForOwnerOnlyTypes(req.getPostType(), group, userId);
            }
            post.setPostType(req.getPostType());
        }

        // 권한 체크
        if (post.getPostType() == GroupPost.PostType.REVIEW) {
            if (!post.getUser().getId().equals(userId) && !isOwner(group, userId)) {
                throw new IllegalStateException("게시글 수정 권한이 없습니다.");
            }
        } else {
            if (!isOwner(group, userId)) {
                throw new IllegalStateException("공지/공동 목표/추천 도서는 그룹장만 수정할 수 있습니다.");
            }
        }

        if (req.getTitle() != null)   post.setTitle(req.getTitle());
        if (req.getContent() != null) post.setContent(req.getContent());

        if (req.getImageUrls() != null) {
            replaceImagesWithUrls(post, req.getImageUrls());
        }

        GroupPost saved = groupPostRepository.save(post);
        return GroupPostDto.fromEntity(saved);
    }

    /* ========================
       이미지: 여러 장 추가(append)
       ======================== */
    @Transactional
    public GroupPostDto addImages(Long postId, Long userId, List<MultipartFile> files) {
        GroupPost post = groupPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        Group group = post.getGroup();
        // 권한
        if (post.getPostType() == GroupPost.PostType.REVIEW) {
            if (!post.getUser().getId().equals(userId) && !isOwner(group, userId)) {
                throw new IllegalStateException("이미지 추가 권한이 없습니다.");
            }
        } else {
            if (!isOwner(group, userId)) {
                throw new IllegalStateException("이미지 추가는 그룹장만 가능합니다.");
            }
        }

        int startOrder = post.getImages().size();
        if (files != null) {
            for (MultipartFile f : files) {
                String objectName = buildGroupPostObjectName(group.getId(), post.getId(), f); // 확장자 포함
                String url = storageService.upload(f, objectName); // objectName 그대로 사용/반환
                post.getImages().add(
                        GroupPostImage.builder()
                                .post(post)
                                .imageUrl(url)
                                .sortOrder(startOrder++)
                                .build()
                );
            }
        }

        GroupPost saved = groupPostRepository.save(post);
        return GroupPostDto.fromEntity(saved);
    }

    /* ========================
       이미지: 전체 교체(replace)
       ======================== */
    @Transactional
    public GroupPostDto replaceImages(Long postId, Long userId, List<MultipartFile> files) {
        GroupPost post = groupPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        Group group = post.getGroup();
        // 권한
        if (post.getPostType() == GroupPost.PostType.REVIEW) {
            if (!post.getUser().getId().equals(userId) && !isOwner(group, userId)) {
                throw new IllegalStateException("이미지 교체 권한이 없습니다.");
            }
        } else {
            if (!isOwner(group, userId)) {
                throw new IllegalStateException("이미지 교체는 그룹장만 가능합니다.");
            }
        }

        // 기존 모두 제거 (orphanRemoval=true)
        post.getImages().clear();

        int order = 0;
        if (files != null) {
            for (MultipartFile f : files) {
                String objectName = buildGroupPostObjectName(group.getId(), post.getId(), f); // 확장자 포함
                String url = storageService.upload(f, objectName);
                post.getImages().add(
                        GroupPostImage.builder()
                                .post(post)
                                .imageUrl(url)
                                .sortOrder(order++)
                                .build()
                );
            }
        }

        GroupPost saved = groupPostRepository.save(post);
        return GroupPostDto.fromEntity(saved);
    }

    /* ========================
       이미지: 특정 한 장 삭제
       ======================== */
    @Transactional
    public GroupPostDto removeImage(Long postId, Long userId, Long imageId) {
        GroupPost post = groupPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        Group group = post.getGroup();
        if (post.getPostType() == GroupPost.PostType.REVIEW) {
            if (!post.getUser().getId().equals(userId) && !isOwner(group, userId)) {
                throw new IllegalStateException("이미지 삭제 권한이 없습니다.");
            }
        } else {
            if (!isOwner(group, userId)) {
                throw new IllegalStateException("이미지 삭제는 그룹장만 가능합니다.");
            }
        }

        post.setImages(
                post.getImages().stream()
                        .filter(img -> !Objects.equals(img.getId(), imageId))
                        .collect(Collectors.toList())
        );

        GroupPost saved = groupPostRepository.save(post);
        return GroupPostDto.fromEntity(saved);
    }

    /* ---------- 내부 헬퍼: URL 리스트로 이미지 교체 ---------- */
    private void replaceImagesWithUrls(GroupPost post, List<String> urls) {
        post.getImages().clear();
        int order = 0;
        if (urls != null) {
            for (String u : urls) {
                if (u == null || u.isBlank()) continue;
                post.getImages().add(
                        GroupPostImage.builder()
                                .post(post)
                                .imageUrl(u.trim())
                                .sortOrder(order++)
                                .build()
                );
            }
        }
    }

    /* ========================
       게시글 삭제
       ======================== */
    @Transactional
    public void deletePost(Long groupId, Long postId, Long userId) {
        GroupPost post = groupPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        if (!Objects.equals(post.getGroup().getId(), groupId)) {
            throw new IllegalArgumentException("그룹이 일치하지 않습니다.");
        }

        Group group = post.getGroup();
        boolean isOwner = group.getOwner() != null && Objects.equals(group.getOwner().getId(), userId);

        switch (post.getPostType()) {
            case REVIEW -> {
                boolean isAuthor = Objects.equals(post.getUser().getId(), userId);
                if (!isAuthor && !isOwner) {
                    throw new IllegalStateException("삭제 권한이 없습니다.");
                }
            }
            case NOTICE, GOAL_SHARE, RECOMMENDED_BOOK -> {
                if (!isOwner) {
                    throw new IllegalStateException("공지/공동목표/추천도서는 그룹장만 삭제할 수 있습니다.");
                }
            }
            default -> throw new IllegalStateException("허용되지 않은 게시글 타입입니다.");
        }

        groupPostRepository.delete(post);
    }

    /* -----------------------------------------
       좋아요 / 좋아요 취소
       ----------------------------------------- */
    @Transactional
    public void likePost(Long postId, Long userId) {
        GroupPost post = groupPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!isInteractive(post.getPostType())) {
            throw new IllegalStateException("이 게시글 유형에서는 좋아요를 사용할 수 없습니다.");
        }

        assertAcceptedMember(post.getGroup(), user);

        if (groupLikeRepository.findByPostAndUser(post, user).isPresent()) {
            throw new IllegalStateException("이미 좋아요를 눌렀습니다.");
        }

        GroupLike like = GroupLike.builder()
                .post(post)
                .user(user)
                .build();
        groupLikeRepository.save(like);

        // ✅ 리뷰 글의 작성자에게 소통/리뷰 공유 배지 갱신
        if (post.getPostType() == GroupPost.PostType.REVIEW) {
            badgeService.evaluateAll(post.getUser().getId());
            // 또는 필요시 아래만:
            // badgeService.evaluateAndAward(BadgeType.REVIEW_SHARE_MASTER, post.getUser().getId());
        }
    }

    @Transactional
    public void unlikePost(Long postId, Long userId) {
        GroupPost post = groupPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!isInteractive(post.getPostType())) {
            throw new IllegalStateException("이 게시글 유형에서는 좋아요를 사용할 수 없습니다.");
        }

        assertAcceptedMember(post.getGroup(), user);

        GroupLike like = groupLikeRepository.findByPostAndUser(post, user)
                .orElseThrow(() -> new IllegalStateException("좋아요를 누르지 않았습니다."));
        groupLikeRepository.delete(like);

        // 언라이크 시에는 배지 강등 로직이 없다면 굳이 재평가 안 해도 됨(정책에 따라 유지)
    }

    /* -----------------------------------------
       단건 조회 (좋아요·댓글 표시 규칙 포함)
       ----------------------------------------- */
    @Transactional(readOnly = true)
    public GroupPostDto getPost(Long postId, Long currentUserId) {
        GroupPost post = groupPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        GroupPostDto dto = GroupPostDto.fromEntity(post);

        if (!isInteractive(post.getPostType())) {
            dto.setLikeCount(0);
            dto.setLikedByMe(false);
            dto.setLikedUsers(Collections.emptyList());
            dto.setCommentCount(0);
            return dto;
        }

        int likeCount = groupLikeRepository.countByPost(post);
        boolean likedByMe = currentUserId != null
                && groupLikeRepository.existsByPost_IdAndUser_Id(post.getId(), currentUserId);
        List<LikerDto> likedUsers = groupLikeRepository.findByPostOrderByIdDesc(post).stream()
                .map(gl -> LikerDto.builder()
                        .userId(gl.getUser().getId())
                        .nickname(gl.getUser().getNickname())
                        .profileImageUrl(gl.getUser().getProfileImageUrl())
                        .build())
                .collect(Collectors.toList());

        int commentCount = groupCommentRepository.countByPost(post);

        dto.setLikeCount(likeCount);
        dto.setLikedByMe(likedByMe);
        dto.setLikedUsers(likedUsers);
        dto.setCommentCount(commentCount);
        return dto;
    }
}
