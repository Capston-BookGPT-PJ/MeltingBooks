package com.example.project.service.badge.rules;

import com.example.project.enums.BadgeTier;
import com.example.project.enums.BadgeType;
import com.example.project.repository.StatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GenreMasterBadgeRule implements BadgeRule {

    private final StatsRepository stats;

    @Override public BadgeType supports() { return BadgeType.GENRE_MASTER; }

    @Override
    public BadgeTier evaluate(Long userId) {
        // 예시: 한 장르에서의 완독 수 (임시)
        String genre = "DEFAULT";
        long cnt = stats.countFinishedBooksByGenre(userId, genre);

        if (cnt >= 3 && cnt < 5) return BadgeTier.BRONZE;
        if (cnt >= 5 && cnt < 10) return BadgeTier.SILVER;

        if (cnt >= 10) {
            // ✅ 서로 다른 장르 중, 각 5권 이상 완독한 장르가 3개 이상이면 PLATINUM
            long genresWith5 = stats.countGenresWithMinFinished(userId, 5);
            if (genresWith5 >= 3) return BadgeTier.PLATINUM;
            return BadgeTier.GOLD;
        }
        return null;
    }
}

