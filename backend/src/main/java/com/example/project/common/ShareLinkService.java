package com.example.project.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ShareLinkService {

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    public String reviewUrl(Long reviewId) {
        return String.format("%s/reviews/%d", frontendBaseUrl, reviewId);
    }
}
