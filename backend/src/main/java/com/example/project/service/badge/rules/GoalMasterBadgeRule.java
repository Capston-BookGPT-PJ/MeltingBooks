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
public class GoalMasterBadgeRule implements BadgeRule {

    private final StatsRepository stats;

    @Override public BadgeType supports() { return BadgeType.GOAL_MASTER; }

    @Override
    public BadgeTier evaluate(Long userId) {
        long achieved = stats.countAchievedMonthlyGoals(userId); // 누적 달성 월 수
        if (achieved >= 1 && achieved < 2) return BadgeTier.BRONZE;
        if (achieved >= 2 && achieved < 3) return BadgeTier.SILVER;

        LocalDate now = LocalDate.now();
        // now 포함 직전부터 '연속 3개월'이 모두 달성인지 검사 (경계 포함 여부는 유틸 확인)
        boolean gold = DateRuleUtils.isConsecutiveMonthsSatisfied(now, 3, ym ->
                stats.isMonthlyGoalAchieved(userId, ym)
        );
        if (gold) {
            long groupAchieved = stats.countAchievedGroupGoals(userId);
            if (groupAchieved >= 1) return BadgeTier.PLATINUM; // 그룹 목표도 달성
            return BadgeTier.GOLD;
        }
        return null;
    }
}

