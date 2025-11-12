package com.example.project.service.badge.rules;

import com.example.project.enums.BadgeTier;
import com.example.project.enums.BadgeType;
import com.example.project.repository.StatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MarathonerBadgeRule implements BadgeRule {

    private final StatsRepository stats;

    @Override
    public BadgeType supports() {
        return BadgeType.MARATHONER; // ✅ enum에 MARATHONER가 있어야 함
    }

    @Override
    public BadgeTier evaluate(Long userId) {
        int thisYear = LocalDate.now().getYear();

        // 현재 연간 목표/진행 가져오기
        Optional<Integer> yearlyTargetOpt = stats.getYearlyTarget(userId, thisYear);
        if (yearlyTargetOpt.isEmpty()) {
            return null; // 연간 목표가 없다면 배지 부여 X
        }
        int target = yearlyTargetOpt.get();
        if (target <= 0) {
            return null; // 0 또는 음수면 무의미
        }
        long progress = stats.getYearlyProgress(userId, thisYear);

        // 브론즈: 연간 목표의 절반 달성(이상)
        if (progress * 1.0 >= target * 0.5 && progress < target) {
            return BadgeTier.BRONZE;
        }

        // 실버: 연간 목표 달성(이상)
        if (progress >= target) {
            // 골드: 2년 연속 연간 목표 달성 (올해 + 작년)
            int lastYear = thisYear - 1;
            boolean lastYearAchieved = stats.getYearlyTarget(userId, lastYear)
                    .filter(t -> t > 0)
                    .map(t -> stats.getYearlyProgress(userId, lastYear) >= t)
                    .orElse(false);

            if (lastYearAchieved) {
                // 플래티넘: 골드 + 그룹 공동 독서 목표 5회 이상 완료
                long groupGoals = stats.countAchievedGroupGoals(userId);
                if (groupGoals >= 5) {
                    return BadgeTier.PLATINUM;
                }
                return BadgeTier.GOLD;
            }

            return BadgeTier.SILVER;
        }

        return null; // 조건 미충족
    }
}
