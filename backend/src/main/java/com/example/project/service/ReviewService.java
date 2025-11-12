package com.example.project.service;

import com.example.project.common.ShareLinkService;
import com.example.project.dto.LikerDto;
import com.example.project.dto.PopularHashtagDto;
import com.example.project.dto.ReviewDto;
import com.example.project.entity.*;
import com.example.project.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final HashtagRepository hashtagRepository;
    private final StorageService storageService;
    private final ReviewHashtagRepository reviewHashtagRepository;

    private final ShareLinkService shareLinkService;
    private final ReadingGoalService readingGoalService;

    // ✅ 좋아요 정보 조립
    private final LikeRepository likeRepository;

    // ---------------------------
    // 내부 유틸
    // ---------------------------
    private String normalizeTag(String raw) {
        if (raw == null) return null;
        String t = raw.trim();
        if (t.startsWith("#")) t = t.substring(1);
        return t;
    }

    private String normalizeTagForQuery(String raw) {
        if (raw == null) return null;
        String t = raw.trim();
        if (t.startsWith("#")) t = t.substring(1);
        return t;
    }

    private void linkHashtags(Review review, List<String> hashtagNames) {
        if (hashtagNames == null) return;
        Set<Long> existing = review.getHashtags().stream()
                .map(rh -> rh.getHashtag().getHashtagId())
                .collect(Collectors.toSet());

        hashtagNames.stream()
                .filter(Objects::nonNull)
                .map(this::normalizeTag)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .forEach(name -> {
                    Hashtag tag = hashtagRepository.findByTagText(name)
                            .orElseGet(() -> hashtagRepository.save(Hashtag.builder().tagText(name).build()));
                    if (!existing.contains(tag.getHashtagId())) {
                        ReviewHashtag rh = ReviewHashtag.builder()
                                .review(review)
                                .hashtag(tag)
                                .build();
                        review.getHashtags().add(rh);
                    }
                });
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

    /** ✅ @RequestAttribute가 없을 때 SecurityContext에서 폴백 */
    private Long resolveViewerId(Long viewerId) {
        if (viewerId != null) return viewerId;
        var ctx = SecurityContextHolder.getContext();
        if (ctx == null) return null;
        Authentication auth = ctx.getAuthentication();
        if (auth == null) return null;
        Object principal = auth.getPrincipal();
        try {
            var m = principal.getClass().getMethod("getId");
            Object id = m.invoke(principal);
            return (id instanceof Long) ? (Long) id : null;
        } catch (Exception ignore) {
            return null;
        }
    }

    // ---------------------------
    // 생성/수정
    // ---------------------------
    @Transactional
    public ReviewDto create(Long userId, Long bookId, String content, Integer rating,
                            List<MultipartFile> files, List<String> hashtagNames, List<String> imageUrls) {

        if (content == null || content.isBlank()) {
            throw new RuntimeException("리뷰 내용을 입력해주세요.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Book book = null;
        if (bookId != null) {
            book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new RuntimeException("책을 찾을 수 없습니다."));
        }

        List<String> urls = new ArrayList<>(imageUrls != null ? imageUrls : List.of());
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                String fileName = "review/" + UUID.randomUUID();
                String uploadedUrl = storageService.upload(file, fileName);
                urls.add(uploadedUrl);
            }
        }
        urls = mergeImageUrls(urls, null);

        Review review = Review.builder()
                .user(user)
                .book(book)
                .content(content)
                .rating(rating)
                .reviewImageUrls(urls)
                .hashtags(new ArrayList<>())
                .build();

        linkHashtags(review, hashtagNames);

        Review saved = reviewRepository.save(review);

        // 생성 직후 목표 업데이트
        readingGoalService.onReviewCreated(userId);

        String shareUrl = shareLinkService.reviewUrl(saved.getId());
        return ReviewDto.from(saved, shareUrl);
    }

    @Transactional
    public ReviewDto uploadReviewImages(Long reviewId, List<MultipartFile> files) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                String fileName = "review/" + reviewId + "_" + UUID.randomUUID();
                String uploadedUrl = storageService.upload(file, fileName);
                review.getReviewImageUrls().add(uploadedUrl);
            }
            review.setReviewImageUrls(mergeImageUrls(review.getReviewImageUrls(), null));
        }

        Review saved = reviewRepository.save(review);
        String shareUrl = shareLinkService.reviewUrl(saved.getId());
        return ReviewDto.from(saved, shareUrl);
    }

    @Transactional
    public ReviewDto update(Long reviewId, Long userId, String content, Integer rating,
                            List<String> hashtagNames, List<String> imageUrls,
                            List<MultipartFile> files, boolean clearImages, Long bookId) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));
        if (!Objects.equals(review.getUser().getId(), userId)) {
            throw new RuntimeException("FORBIDDEN");
        }

        if (content != null) review.setContent(content);
        if (rating != null) review.setRating(rating);

        if (bookId != null) {
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new RuntimeException("책을 찾을 수 없습니다."));
            review.setBook(book);
        }

        if (clearImages) {
            review.setReviewImageUrls(new ArrayList<>());
        }
        if (imageUrls != null) {
            review.setReviewImageUrls(mergeImageUrls(review.getReviewImageUrls(), imageUrls));
        }
        if (files != null && !files.isEmpty()) {
            List<String> uploaded = new ArrayList<>();
            for (MultipartFile file : files) {
                String fileName = "review/" + reviewId + "_" + UUID.randomUUID();
                String uploadedUrl = storageService.upload(file, fileName);
                uploaded.add(uploadedUrl);
            }
            review.setReviewImageUrls(mergeImageUrls(review.getReviewImageUrls(), uploaded));
        }

        if (hashtagNames != null) {
            reviewHashtagRepository.deleteByReview(review);
            review.getHashtags().clear();
            reviewRepository.flush();

            hashtagNames.stream()
                    .filter(Objects::nonNull)
                    .map(this::normalizeTag)
                    .filter(s -> s != null && !s.isBlank())
                    .distinct()
                    .forEach(name -> {
                        Hashtag tag = hashtagRepository.findByTagText(name)
                                .orElseGet(() -> hashtagRepository.save(Hashtag.builder().tagText(name).build()));
                        ReviewHashtag rh = ReviewHashtag.builder()
                                .review(review)
                                .hashtag(tag)
                                .build();
                        review.getHashtags().add(rh);
                    });
        }

        Review saved = reviewRepository.save(review);
        String shareUrl = shareLinkService.reviewUrl(saved.getId());
        return ReviewDto.from(saved, shareUrl);
    }

    // ---------------------------
    // 조회 (좋아요 정보 포함)
    // ---------------------------
    @Transactional(readOnly = true)
    public ReviewDto get(Long reviewId, Long viewerId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));
        String shareUrl = shareLinkService.reviewUrl(reviewId);
        return toDtoWithLikeInfo(review, shareUrl, resolveViewerId(viewerId));
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> getAll(Long viewerId) {
        Long v = resolveViewerId(viewerId);
        return reviewRepository.findAll().stream()
                .map(r -> toDtoWithLikeInfo(r, shareLinkService.reviewUrl(r.getId()), v))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> getByUser(Long ownerUserId, Long viewerId) {
        Long v = resolveViewerId(viewerId);
        User user = userRepository.findById(ownerUserId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return reviewRepository.findByUser(user).stream()
                .map(r -> toDtoWithLikeInfo(r, shareLinkService.reviewUrl(r.getId()), v))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> getByBook(Long bookId, Long viewerId) {
        Long v = resolveViewerId(viewerId);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("책을 찾을 수 없습니다."));
        return reviewRepository.findByBook(book).stream()
                .map(r -> toDtoWithLikeInfo(r, shareLinkService.reviewUrl(r.getId()), v))
                .collect(Collectors.toList());
    }

    // ---------------------------
    // 인기 해시태그 Top 20
    // ---------------------------
    @Transactional(readOnly = true)
    public List<PopularHashtagDto> getPopularHashtagsTop20() {
        Page<ReviewHashtagRepository.PopularHashtagProjection> page =
                reviewHashtagRepository.findPopularTags(PageRequest.of(0, 20));
        return page.getContent().stream()
                .map(p -> PopularHashtagDto.builder()
                        .tag("#" + p.getTag())
                        .count(p.getCnt())
                        .build())
                .toList();
    }

    // ---------------------------
    // 해시태그 검색 (좋아요 포함)
    // ---------------------------
    @Transactional(readOnly = true)
    public Page<ReviewDto> searchReviewsByHashtag(String hashtag, int page, int size, Long viewerId) {
        Long v = resolveViewerId(viewerId);
        String norm = normalizeTagForQuery(hashtag);
        PageRequest pr = PageRequest.of(Math.max(0, page), Math.max(1, size));
        Page<Review> result = reviewHashtagRepository.findReviewsByTagOrderByCreatedAtDesc(norm, pr);
        return result.map(r -> toDtoWithLikeInfo(r, shareLinkService.reviewUrl(r.getId()), v));
    }

    // ---------------------------
    // 삭제
    // ---------------------------
    @Transactional
    public void delete(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));
        Long ownerId = review.getUser().getId();
        if (!Objects.equals(ownerId, userId)) {
            throw new RuntimeException("FORBIDDEN");
        }
        reviewRepository.delete(review);
    }

    // ---------------------------
    // 내부: like 정보 조립
    // ---------------------------
    private ReviewDto toDtoWithLikeInfo(Review review, String shareUrl, Long viewerId) {
        Long reviewId = review.getId();

        long likeCount = likeRepository.countByReview(review);
        boolean likedByMe = (viewerId != null) && likeRepository.existsByUser_IdAndReview_Id(viewerId, reviewId);
        List<LikerDto> likedUsers = likeRepository.findRecentLikersOfReview(reviewId, PageRequest.of(0, 5));
        if (likedUsers == null) likedUsers = List.of();

        // 목록에 내가 있으면 보정
        if (!likedByMe && viewerId != null && likedUsers.stream().anyMatch(l -> Objects.equals(l.getUserId(), viewerId))) {
            likedByMe = true;
        }

        return ReviewDto.fromWithLikeInfo(review, shareUrl, likeCount, likedByMe, likedUsers);
    }
}
