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
public class FullReadBadgeRule implements BadgeRule {

    private final StatsRepository stats;

    @Override public BadgeType supports() { return BadgeType.FULL_READ; }

    @Override
    public BadgeTier evaluate(Long userId) {
        long total = stats.countTotalFinishedBooks(userId); // finished=true, distinct book 기준

        // 최근 3개월 연속: 매달 1권 이상
        LocalDate now = LocalDate.now();
        boolean threeConsecutiveMonths = DateRuleUtils.isConsecutiveMonthsSatisfied(now, 3, ym ->
                stats.countFinishedBooks(
                        userId,
                        ym.atDay(1),           // LocalDate
                        ym.atEndOfMonth()      // LocalDate
                ) >= 1
        );

        // 상위 티어부터 체크
        if (total >= 10 && threeConsecutiveMonths) return BadgeTier.PLATINUM;
        if (total >= 10) return BadgeTier.GOLD;
        if (total >= 5)  return BadgeTier.SILVER;
        if (total >= 1)  return BadgeTier.BRONZE;

        return null;
    }
}
