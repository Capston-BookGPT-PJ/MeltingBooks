package com.example.project.service;

import com.example.project.entity.ExpEvent;
import com.example.project.entity.User;
import com.example.project.enums.ExpEventType;
import com.example.project.repository.ExpEventRepository;
import com.example.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class LevelService {

    private final UserRepository userRepository;
    private final ExpEventRepository expEventRepository;

    private static final int BASE_EXP_LEVEL_2 = 300;
    private static final int TIER_1_LEVEL_END = 49;
    private static final double TIER_1_MULTIPLIER = 10.0;
    private static final double TIER_2_MULTIPLIER = 30.0;

    // ---------------------------------
    // 1️⃣ 이벤트 기반 경험치 지급
    // ---------------------------------
    @Transactional
    public String giveExp(Long userId, ExpEventType eventType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (isAlreadyGiven(user, eventType)) {
            return "이미 지급된 이벤트입니다: " + eventType;
        }

        int pointsToAdd = eventType.getPoints();
        user.setExperience(user.getExperience() + pointsToAdd);
        checkLevelUp(user);

        ExpEvent expEvent = ExpEvent.of(user, eventType);
        expEventRepository.save(expEvent);
        userRepository.save(user);

        return "경험치 지급 완료: " + pointsToAdd + " EXP (" + eventType + ")";
    }

    // ---------------------------------
    // 2️⃣ 관리자 테스트용: 단순 경험치 주입
    // ---------------------------------
    @Transactional
    public String addExperience(Long userId, int points) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setExperience(user.getExperience() + points);
        checkLevelUp(user);

        userRepository.save(user);

        return "관리자 포인트 주입 완료: " + points + " EXP";
    }

    // ---------------------------------
    // 중복 지급/기간 체크
    // ---------------------------------
    private boolean isAlreadyGiven(User user, ExpEventType eventType) {
        switch (eventType) {
            case ACHIEVE_MONTHLY_GOAL:
                YearMonth thisMonth = YearMonth.now();
                LocalDateTime startOfMonth = thisMonth.atDay(1).atStartOfDay();
                LocalDateTime endOfMonth = thisMonth.atEndOfMonth().atTime(23, 59, 59);
                return expEventRepository.findByUserAndEventTypeAndCreatedAtBetween(
                        user, eventType, startOfMonth, endOfMonth
                ).isPresent();

            case ACHIEVE_YEARLY_GOAL:
                LocalDateTime startOfYear = LocalDateTime.of(LocalDateTime.now().getYear(), 1, 1, 0, 0);
                LocalDateTime endOfYear = LocalDateTime.of(LocalDateTime.now().getYear(), 12, 31, 23, 59);
                return expEventRepository.findByUserAndEventTypeAndCreatedAtBetween(
                        user, eventType, startOfYear, endOfYear
                ).isPresent();

            default:
                return expEventRepository.findByUserAndEventType(user, eventType).isPresent();
        }
    }

    // ---------------------------------
    // 레벨업 계산
    // ---------------------------------
    private void checkLevelUp(User user) {
        int currentLevel = user.getLevel();
        int currentExp = user.getExperience();

        while (currentExp >= getRequiredExpForNextLevel(currentLevel)) {
            currentExp -= getRequiredExpForNextLevel(currentLevel);
            currentLevel += 1;
        }

        user.setLevel(currentLevel);
        user.setExperience(currentExp);
    }

    public int getRequiredExpForNextLevel(int currentLevel) {
        if (currentLevel == 1) return BASE_EXP_LEVEL_2;
        if (currentLevel < TIER_1_LEVEL_END) {
            return (int) (TIER_1_MULTIPLIER * Math.pow(currentLevel - 1, 1.2));
        }
        return (int) (TIER_2_MULTIPLIER * Math.pow(currentLevel - 1, 1.2));
    }
}
