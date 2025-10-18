package com.example.project.dto;

import com.example.project.entity.Badge;
import com.example.project.enums.BadgeTier;
import com.example.project.enums.BadgeType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BadgeDto {
    private Long id;
    private BadgeType badgeType;
    private BadgeTier tier;
    private String imageUrl;
    private LocalDateTime createdAt;

    public static BadgeDto from(Badge badge) {
        return BadgeDto.builder()
                .id(badge.getId())
                .badgeType(badge.getBadgeType())
                .tier(badge.getTier())
                .imageUrl(badge.getImageUrl())
                .createdAt(badge.getCreatedAt())
                .build();
    }
}
