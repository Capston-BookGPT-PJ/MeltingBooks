package com.example.project.service.badge;

import com.example.project.util.BadgeImageResolver;
import com.example.project.dto.BadgeListItemDto;
import com.example.project.service.badge.rules.BadgeRule;
import com.example.project.entity.Badge;
import com.example.project.entity.BadgeHistory;
import com.example.project.entity.User;
import com.example.project.enums.BadgeTier;
import com.example.project.enums.BadgeType;
import com.example.project.repository.BadgeHistoryRepository;
import com.example.project.repository.BadgeRepository;
import com.example.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 룰 기반 배지 평가/수여 서비스.
 * - 각 배지 타입 규칙은 BadgeRule 구현체로 분리되어 주입됨.
 * - 이미지 URL은 BadgeImageResolver로 생성.
 */
@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final BadgeHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final BadgeImageResolver imageResolver;

    /** 스프링이 컴포넌트 스캔으로 주입하는 룰 목록 */
    private final List<BadgeRule> rules;

    /** 단일 배지 타입 평가/지급 */
    @Transactional
    public Optional<Badge> evaluateAndAward(BadgeType type, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));

        BadgeRule rule = rules.stream()
                .filter(r -> r.supports() == type)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No rule for type: " + type));

        BadgeTier desired = rule.evaluate(userId);
        if (desired == null) {
            // 기준 미달 → 변화 없음
            return Optional.empty();
        }

        Badge current = badgeRepository.findByUserAndBadgeType(user, type).orElse(null);

        if (current == null) {
            // 최초 수여 경로
            Badge newBadge = Badge.builder()
                    .user(user)
                    .badgeType(type)
                    .tier(desired)
                    .imageUrl(imageResolver.resolve(type, desired))
                    .build();
            try {
                Badge saved = badgeRepository.save(newBadge);
                historyRepository.save(BadgeHistory.builder()
                        .user(user).badgeType(type)
                        .fromTier(null).toTier(desired)
                        .reason("initial award")
                        .build());
                return Optional.of(saved);
            } catch (DataIntegrityViolationException e) {
                // 동시 삽입 레이스: 재조회 후 승급 판단
                Badge existing = badgeRepository.findByUserAndBadgeType(user, type).orElse(null);
                if (existing == null) {
                    // 매우 드문 케이스: 다음 호출에서 다시 처리
                    return Optional.empty();
                }
                if (desired.higherThan(existing.getTier())) {
                    BadgeTier before = existing.getTier();
                    existing.setTier(desired);
                    existing.setImageUrl(imageResolver.resolve(type, desired));
                    historyRepository.save(BadgeHistory.builder()
                            .user(user).badgeType(type)
                            .fromTier(before).toTier(desired)
                            .reason("upgrade (race resolved)")
                            .build());
                    return Optional.of(existing);
                }
                return Optional.empty();
            }
        } else if (desired.higherThan(current.getTier())) {
            // 승급 경로
            BadgeTier before = current.getTier();
            current.setTier(desired);
            current.setImageUrl(imageResolver.resolve(type, desired));
            historyRepository.save(BadgeHistory.builder()
                    .user(user).badgeType(type)
                    .fromTier(before).toTier(desired)
                    .reason("upgrade")
                    .build());
            return Optional.of(current);
        }

        // 변화 없음
        return Optional.empty();
    }

    /** 전체 배지 일괄 평가/지급 */
    @Transactional
    public List<Badge> evaluateAll(Long userId) {
        List<Badge> changed = new ArrayList<>();
        for (BadgeRule rule : rules) {
            evaluateAndAward(rule.supports(), userId).ifPresent(changed::add);
        }
        return changed;
    }

    /** 보유 배지 조회 */
    @Transactional(readOnly = true)
    public List<Badge> getUserBadges(Long userId) {
        return badgeRepository.findAllByUserId(userId);
    }
    
    /** 전체 배지 카탈로그(미획득 포함) */
    @Transactional(readOnly = true)
    public List<BadgeListItemDto> getBadgeCatalog(Long userId) {
        // 유저 보유 배지 -> 맵으로
        var owned = badgeRepository.findAllByUserId(userId).stream()
                .collect(java.util.stream.Collectors.toMap(b -> b.getBadgeType(), b -> b));

        var list = new java.util.ArrayList<BadgeListItemDto>();
        for (BadgeType type : BadgeType.values()) {
            var badge = owned.get(type);
            if (badge != null) {
                // 획득한 배지
                list.add(BadgeListItemDto.builder()
                        .badgeType(type)
                        .obtained(true)
                        .tier(badge.getTier())
                        .imageUrl(badge.getImageUrl())      // 저장된 이미지
                        .build());
            } else {
                // 미획득 배지
                // ▶ 잠금 이미지가 있으면 그걸 쓰고,
                // 없으면 브론즈 프리뷰를 보여주자(UX 관점에서).
                String previewUrl;
                try {
                    // 잠금 이미지가 있다면: badges/<type>-locked.png 규칙 사용 (선택)
                    previewUrl = imageResolver.resolve(type, BadgeTier.BRONZE); // 잠금 이미지 없으면 브론즈 프리뷰
                } catch (Exception e) {
                    previewUrl = imageResolver.resolve(type, BadgeTier.BRONZE);
                }

                list.add(BadgeListItemDto.builder()
                        .badgeType(type)
                        .obtained(false)
                        .tier(null)
                        .imageUrl(previewUrl)
                        .build());
            }
        }
        // 정렬(옵션): 획득한 것 먼저, 그 다음 미획득
        list.sort(java.util.Comparator
                .comparing(BadgeListItemDto::isObtained).reversed()
                .thenComparing(item -> item.getBadgeType().ordinal()));
        return list;
    }
}
