package com.example.project.dto;

import com.example.project.enums.BadgeTier;
import com.example.project.enums.BadgeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class BadgeListItemDto {
    private BadgeType badgeType;   // 배지 종류
    private boolean obtained;      // 획득 여부
    private BadgeTier tier;        // 현재 티어(미획득이면 null)
    private String imageUrl;       // 표시용 이미지 URL
}
