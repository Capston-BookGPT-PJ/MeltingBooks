package com.example.project.service;

import com.example.project.entity.RefreshToken;
import com.example.project.repository.RefreshTokenRepository;
import com.example.project.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthTokenService {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    /** 로그인/콜백 성공 시 호출: 액세스+리프레시 발급 및 리프레시 저장 */
    @Transactional
    public TokenPair issueTokens(Long userId) {
        String access = jwtUtil.generateAccessToken(userId);
        String refresh = jwtUtil.generateRefreshToken(userId);

        // JWT 만료 가져와 DB expiresAt 세팅 (JwtUtil 수정 없이 calculate)
        Instant refreshExpiry = Instant.ofEpochMilli(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7);

        RefreshToken entity = RefreshToken.builder()
                .token(refresh)
                .userId(userId)
                .expiresAt(refreshExpiry)
                .revoked(false)
                .build();
        refreshTokenRepository.save(entity);

        return new TokenPair(access, refresh);
    }

    /** 리프레시로 재발급(회전). 이전 리프레시 revoke */
    @Transactional
    public TokenPair rotateRefresh(String refreshToken) {
        // 1) 형식/서명/만료 검증
        Long userId = jwtUtil.validateToken(refreshToken);
        if (userId == null) throw new IllegalArgumentException("Invalid refresh");

        // 2) DB에 존재하는지 + revoke 여부 확인
        RefreshToken stored = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Refresh not found"));
        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalStateException("Refresh expired or revoked");
        }

        // 3) 새 토큰 발급
        String newAccess = jwtUtil.generateAccessToken(userId);
        String newRefresh = jwtUtil.generateRefreshToken(userId);

        // 4) 기존 토큰 revoke + replacedBy 기록
        stored.setRevoked(true);
        stored.setReplacedByToken(newRefresh);

        // 5) 새 리프레시 저장
        Instant newExpiry = Instant.ofEpochMilli(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7);
        RefreshToken next = RefreshToken.builder()
                .token(newRefresh)
                .userId(userId)
                .expiresAt(newExpiry)
                .revoked(false)
                .build();
        refreshTokenRepository.save(next);

        return new TokenPair(newAccess, newRefresh);
    }

    /** 로그아웃: 현재 리프레시만 무효화 */
    @Transactional
    public void revokeRefresh(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken).ifPresent(rt -> {
            rt.setRevoked(true);
            // rt.setReplacedByToken("logout"); // (선택) 감사 목적으로
        });
    }

    /** 모든 기기 로그아웃(선택): 해당 유저의 리프레시 전부 삭제/무효화 */
    @Transactional
    public void revokeAllByUser(Long userId) {
        // 삭제 대신 revoke만 하려면 findAllByUserId 후 setRevoked(true)
        refreshTokenRepository.deleteByUserId(userId);
    }

    /** 간단 DTO */
    public record TokenPair(String accessToken, String refreshToken) {}
}
