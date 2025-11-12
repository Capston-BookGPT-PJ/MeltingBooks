package com.example.project.service.badge.rules;

import com.example.project.enums.BadgeTier;
import com.example.project.enums.BadgeType;
import com.example.project.repository.StatsRepository;
import com.example.project.util.DateRuleUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ReviewMasterBadgeRule implements BadgeRule {

    private final StatsRepository stats;

    @Override
    public BadgeType supports() {
        return BadgeType.REVIEW_MASTER; // ✅ enum에 REVIEW_MASTER가 있어야 함
    }

    @Override
    public BadgeTier evaluate(Long userId) {
        long totalReviews = stats.countReviews(userId);

        // 브론즈: 감상문 3개 이상
        if (totalReviews >= 3 && totalReviews < 10) {
            return BadgeTier.BRONZE;
        }

        // 실버: 감상문 10개 이상
        if (totalReviews >= 10) {
            // 골드: 3개월 연속 매달 5개 이상의 감상문
            LocalDate now = LocalDate.now();
            boolean gold = DateRuleUtils.isConsecutiveMonthsSatisfied(now, 3, ym ->
                    stats.countMonthlyReviews(userId, ym.getYear(), ym.getMonthValue()) >= 5
            );
            if (gold) {
                // 플래티넘: 골드 + 그룹 내 작성한 감상문 10개 이상
                long groupReviews = stats.countGroupReviews(userId);
                if (groupReviews >= 10) {
                    return BadgeTier.PLATINUM;
                }
                return BadgeTier.GOLD;
            }
            return BadgeTier.SILVER;
        }

        return null; // 조건 미충족
    }
}
