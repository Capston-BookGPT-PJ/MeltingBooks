package com.example.project.util;

import com.example.project.enums.BadgeTier;
import com.example.project.enums.BadgeType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BadgeImageResolver {

    @Value("${badge.base-url}")
    private String baseUrl; 

    public String resolve(BadgeType type, BadgeTier tier) {
        String objectName = String.format("badges/%s-%s.png",
                type.name().toLowerCase(),
                tier.name().toLowerCase());
        return baseUrl + "/" + objectName;
    }
}
