package com.example.project.service.badge.rules;

import com.example.project.enums.BadgeTier;
import com.example.project.enums.BadgeType;

public interface BadgeRule {
    BadgeType supports();
    BadgeTier evaluate(Long userId);
}
