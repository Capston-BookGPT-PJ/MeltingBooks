package com.example.project.controller;

import com.example.project.dto.AuthResponse;
import com.example.project.enums.SocialLoginType;
import com.example.project.service.OAuthService;
import com.example.project.service.AuthTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.servlet.view.RedirectView;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class OAuthController {

    private final OAuthService oAuthService;
    private final AuthTokenService authTokenService; // ⬅️ 추가

    @GetMapping("/{socialLoginType}")
    public RedirectView redirect(@PathVariable SocialLoginType socialLoginType) {
        String redirectUrl = oAuthService.getRedirectUrl(socialLoginType);
        return new RedirectView(redirectUrl);
    }

    @GetMapping("/{socialLoginType}/callback")
    public RedirectView handleCallback(
            @PathVariable SocialLoginType socialLoginType,
            @RequestParam("code") String code
    ) {
        // 기존 로직 유지: 유저 식별까지는 OAuthService가 처리
        AuthResponse response = oAuthService.handleCallback(socialLoginType, code);
        Long userId = response.getUser().getId();

        // ⬇️ 새로 추가: 액세스/리프레시 발급 + 리프레시 DB 저장
        var pair = authTokenService.issueTokens(userId);

        // (웹이면) 리프레시를 HttpOnly 쿠키에 심길 수 있음
        // CookieUtil.addRefreshCookie(httpServletResponse, pair.refreshToken());

        // 모바일 딥링크로 액세스/리프레시 전달 (기존 token 파라미터 대신/추가)
        String clientAppUrl = "meltingbooks://callback";
        String redirectUrl = UriComponentsBuilder.fromUriString(clientAppUrl)
                .queryParam("accessToken", pair.accessToken())
                .queryParam("refreshToken", pair.refreshToken())
                .queryParam("userId", userId)
                .toUriString();

        return new RedirectView(redirectUrl);
    }
}
