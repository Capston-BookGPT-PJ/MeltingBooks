package com.example.project.controller;

import com.example.project.service.AuthTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthTokenController {

    private final AuthTokenService authTokenService;

    /** 리프레시로 토큰 재발급(회전). 바디로 refreshToken 받음 */
    @PostMapping("/token/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@RequestBody RefreshRequest body) {
        if (body == null || body.refreshToken() == null || body.refreshToken().isBlank()) {
            return ResponseEntity.status(401).build();
        }
        var pair = authTokenService.rotateRefresh(body.refreshToken());
        // 모바일은 새 refreshToken도 바디로 내려 클라이언트가 교체 저장
        return ResponseEntity.ok(new TokenResponse(pair.accessToken(), pair.refreshToken()));
    }

    /** 로그아웃: 전달된 리프레시 무효화 */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshRequest body) {
        if (body != null && body.refreshToken() != null && !body.refreshToken().isBlank()) {
            authTokenService.revokeRefresh(body.refreshToken());
        }
        return ResponseEntity.noContent().build();
    }

    // ===== DTOs =====
    public record RefreshRequest(String refreshToken) {}
    public record TokenResponse(String accessToken, String refreshToken) {}
}
