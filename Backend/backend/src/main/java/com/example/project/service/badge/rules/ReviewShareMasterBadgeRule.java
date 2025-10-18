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
public class ReviewShareMasterBadgeRule implements BadgeRule {

    private final StatsRepository stats;

    @Override public BadgeType supports() { return BadgeType.REVIEW_SHARE_MASTER; }

    @Override
    public BadgeTier evaluate(Long userId) {
        long totalComments = stats.countCommentsOnOthersReviews(userId); // 누적
        if (totalComments >= 10 && totalComments < 20) return BadgeTier.BRONZE;

        if (totalComments >= 20) {
            LocalDate now = LocalDate.now();
            // 최근 3개월 연속, 매월 5개 이상 댓글
            boolean gold = DateRuleUtils.isConsecutiveMonthsSatisfied(now, 3, ym ->
                    stats.countMonthlyCommentsOnOthersReviews(userId, ym.getYear(), ym.getMonthValue()) >= 5
            );
            if (gold) {
                long groupComments = stats.countGroupCommentsOnOthersReviews(userId);
                long likes = stats.countLikesReceivedForMyGroupReviews(userId);
                if (groupComments >= 10 && likes >= 5) return BadgeTier.PLATINUM;
                return BadgeTier.GOLD;
            }
            return BadgeTier.SILVER; // 20개 누적은 채웠지만 연속월 요건은 못 채운 경우
        }
        return null;
    }
}
