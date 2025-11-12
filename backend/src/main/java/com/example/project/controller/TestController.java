package com.example.project.controller;

import com.example.project.dto.AuthResponse;
import com.example.project.dto.UserDto;
import com.example.project.service.AuthTokenService;
import com.example.project.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final JwtUtil jwtUtil;                     // /validate 용 (선택)
    private final AuthTokenService authTokenService;   // ✅ DB 저장형 발급

    /** ❗ 기존 방식: DB 미저장 → refresh API와 호환 안 됨 (남겨도 되지만 주의) */
    @Deprecated
    @PostMapping("/token")
    public AuthResponse generateToken(@RequestBody TestRequest request) {
        Long userId = Long.parseLong(request.getUserId());
        String accessToken = jwtUtil.generateAccessToken(userId);
        String refreshToken = jwtUtil.generateRefreshToken(userId);

        return AuthResponse.builder()
                .user(UserDto.builder().id(userId).build())
                .token(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /** ✅ 권장: DB에 저장되는 테스트 발급 API (refresh 플로우와 100% 호환) */
    @PostMapping("/seed")
    public AuthResponse seedTokens(@RequestBody TestRequest request) {
        Long userId = Long.parseLong(request.getUserId());
        var pair = authTokenService.issueTokens(userId); // DB에 저장됨

        return AuthResponse.builder()
                .user(UserDto.builder().id(userId).build())
                .token(pair.accessToken())
                .refreshToken(pair.refreshToken())
                .build();
    }

    /** 토큰 유효성 확인(옵션) */
    @GetMapping("/validate")
    public String validateToken(@RequestParam String token) {
        Long userId = jwtUtil.validateToken(token);
        return "UserId: " + userId;
    }
}

class TestRequest {
    private String userId;
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
