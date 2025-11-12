package com.example.project.service;

import com.example.project.dto.AuthResponse;
import com.example.project.dto.UserDto;
import com.example.project.enums.SocialLoginType;
import com.example.project.entity.User;
import com.example.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private final UserRepository userRepository;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String googleRedirectUri;

    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String naverClientId;
    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String naverClientSecret;
    @Value("${spring.security.oauth2.client.registration.naver.redirect-uri}")
    private String naverRedirectUri;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;
    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String kakaoClientSecret;
    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    /* =========================
       Public
       ========================= */
    public String getRedirectUrl(SocialLoginType socialLoginType) {
        switch (socialLoginType) {
            case GOOGLE:
                return UriComponentsBuilder.fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                        .queryParam("client_id", googleClientId)
                        .queryParam("redirect_uri", googleRedirectUri)
                        .queryParam("response_type", "code")
                        .queryParam("scope", "openid email profile")
                        .toUriString();
            case NAVER:
                return UriComponentsBuilder.fromUriString("https://nid.naver.com/oauth2.0/authorize")
                        .queryParam("client_id", naverClientId)
                        .queryParam("redirect_uri", naverRedirectUri)
                        .queryParam("response_type", "code")
                        .toUriString();
            case KAKAO:
                return UriComponentsBuilder.fromUriString("https://kauth.kakao.com/oauth/authorize")
                        .queryParam("client_id", kakaoClientId)
                        .queryParam("redirect_uri", kakaoRedirectUri)
                        .queryParam("response_type", "code")
                        .toUriString();
            default:
                throw new IllegalArgumentException("Unsupported social login type: " + socialLoginType);
        }
    }

    public AuthResponse handleCallback(SocialLoginType socialLoginType, String code) {
        switch (socialLoginType) {
            case GOOGLE: return handleGoogleCallback(code);
            case NAVER:  return handleNaverCallback(code);
            case KAKAO:  return handleKakaoCallback(code);
            default: throw new IllegalArgumentException("Unsupported social login type: " + socialLoginType);
        }
    }

    /* =========================
       Google
       ========================= */
    private AuthResponse handleGoogleCallback(String code) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, String> tokenRequest = new HashMap<>();
        tokenRequest.put("code", code);
        tokenRequest.put("client_id", googleClientId);
        tokenRequest.put("client_secret", googleClientSecret);
        tokenRequest.put("redirect_uri", googleRedirectUri);
        tokenRequest.put("grant_type", "authorization_code");

        @SuppressWarnings("unchecked")
        Map<String, Object> tokenResponse = restTemplate.postForObject(
                "https://oauth2.googleapis.com/token", tokenRequest, Map.class);

        String accessToken = tokenResponse != null ? (String) tokenResponse.get("access_token") : null;

        @SuppressWarnings("unchecked")
        Map<String, Object> userInfo = restTemplate.getForObject(
                "https://www.googleapis.com/oauth2/v2/userinfo?access_token=" + accessToken,
                Map.class
        );

        String email        = safeString(userInfo, "email");
        String rawNickname  = safeString(userInfo, "name");
        String profileImage = safeString(userInfo, "picture");
        String googleId     = safeString(userInfo, "id");

        String username = buildUsername(email, "google", googleId);
        String nickname = fallbackNickname(rawNickname, username);

        User user = loginOrRegister(email, nickname, username, profileImage, googleId, SocialLoginType.GOOGLE);

        return AuthResponse.builder()
                .user(UserDto.from(user))
                .token(null) // 토큰은 별도 AuthTokenService가 발급한다면 null 유지
                .build();
    }

    /* =========================
       Naver
       ========================= */
    private AuthResponse handleNaverCallback(String code) {
        RestTemplate restTemplate = new RestTemplate();

        String tokenUri = UriComponentsBuilder.fromUriString("https://nid.naver.com/oauth2.0/token")
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", naverClientId)
                .queryParam("client_secret", naverClientSecret)
                .queryParam("redirect_uri", naverRedirectUri)
                .queryParam("code", code)
                .toUriString();

        ResponseEntity<Map> tokenResponse = restTemplate.exchange(tokenUri, HttpMethod.POST, null, Map.class);
        String accessToken = tokenResponse.getBody() != null ? (String) tokenResponse.getBody().get("access_token") : null;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> userInfoResponse = restTemplate.exchange(
                "https://openapi.naver.com/v1/nid/me", HttpMethod.GET, entity, Map.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = userInfoResponse.getBody() != null
                ? (Map<String, Object>) userInfoResponse.getBody().get("response")
                : Collections.emptyMap();

        String naverId      = safeString(response, "id");
        String email        = safeString(response, "email");            // null 가능
        String rawNickname  = safeString(response, "nickname");         // null 가능
        String profileImage = safeString(response, "profile_image");

        String username = buildUsername(email, "naver", naverId);
        String nickname = fallbackNickname(rawNickname, username);

        User user = loginOrRegister(email, nickname, username, profileImage, naverId, SocialLoginType.NAVER);

        return AuthResponse.builder()
                .user(UserDto.from(user))
                .token(null)
                .build();
    }

    /* =========================
       Kakao
       ========================= */
    private AuthResponse handleKakaoCallback(String code) {
        RestTemplate restTemplate = new RestTemplate();

        String tokenUri = UriComponentsBuilder.fromUriString("https://kauth.kakao.com/oauth/token")
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", kakaoClientId)
                .queryParam("client_secret", kakaoClientSecret)
                .queryParam("redirect_uri", kakaoRedirectUri)
                .queryParam("code", code)
                .toUriString();

        ResponseEntity<Map> tokenResponse = restTemplate.exchange(tokenUri, HttpMethod.POST, null, Map.class);
        String accessToken = tokenResponse.getBody() != null ? (String) tokenResponse.getBody().get("access_token") : null;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> userInfoResponse = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me", HttpMethod.GET, entity, Map.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> userInfo = userInfoResponse.getBody() != null ? (Map<String, Object>) userInfoResponse.getBody() : Collections.emptyMap();

        String kakaoId = String.valueOf(userInfo.get("id"));

        @SuppressWarnings("unchecked")
        Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.getOrDefault("kakao_account", Collections.emptyMap());
        String email = (String) kakaoAccount.get("email"); // null 가능

        @SuppressWarnings("unchecked")
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.getOrDefault("profile", Collections.emptyMap());
        String rawNickname  = (String) profile.get("nickname");           // null 가능
        String profileImage = (String) profile.get("profile_image_url");

        String username = buildUsername(email, "kakao", kakaoId);
        String nickname = fallbackNickname(rawNickname, username);

        User user = loginOrRegister(email, nickname, username, profileImage, kakaoId, SocialLoginType.KAKAO);

        return AuthResponse.builder()
                .user(UserDto.from(user))
                .token(null)
                .build();
    }

    /* =========================
       Join / Login
       ========================= */
    private User loginOrRegister(String email,
                                 String nickname,
                                 String username,
                                 String profileImage,
                                 String socialId,
                                 SocialLoginType type) {

        User user = null;
        if (email != null && !email.isBlank()) {
            user = userRepository.findByEmail(email).orElse(null);
        }

        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setUsername(username);                               // NOT NULL
            user.setNickname(fallbackNickname(nickname, username));   // 닉네임 비면 username
            user.setProfileImageUrl(profileImage);
            user.setSocialProviders(new ArrayList<>());
        } else {
            // 닉네임 보정
            if (user.getNickname() == null || user.getNickname().isBlank()) {
                user.setNickname(fallbackNickname(nickname, username));
            }
            // 이메일 보정
            if ((user.getEmail() == null || user.getEmail().isBlank()) && email != null && !email.isBlank()) {
                user.setEmail(email);
            }
            // 프로필 이미지 보정(소셜 기본 이미지가 아닌 경우에만 덮어쓰기)
            if (profileImage != null && !profileImage.isBlank() && !isSocialDefaultImage(profileImage)) {
                user.setProfileImageUrl(profileImage);
            }
        }

        // 로그인 타입/프로바이더 기록 + 소셜 ID 매핑
        user.setLoginType(type);
        if (type != null) {
            user.addSocialProvider(type.name());
        }
        switch (type) {
            case GOOGLE: user.setGoogleId(socialId); break;
            case NAVER:  user.setNaverId(socialId);  break;
            case KAKAO:  user.setKakaoId(socialId);  break;
        }

        return userRepository.save(user);
    }

    /* =========================
       Helpers
       ========================= */
    private static String safeString(Map<String, Object> map, String key) {
        if (map == null) return null;
        Object v = map.get(key);
        return v == null ? null : String.valueOf(v);
    }

    /** 이메일이 없으면 provider_prefix + id로 username 생성 */
    private static String buildUsername(String email, String providerPrefix, String providerId) {
        if (email != null && !email.isBlank() && email.contains("@")) {
            return email.split("@")[0];
        }
        return providerPrefix.toLowerCase() + "_" + providerId; // 예: kakao_123456
    }

    /** 닉네임이 비어있다면 username으로 대체 */
    private static String fallbackNickname(String nickname, String username) {
        return (nickname != null && !nickname.isBlank()) ? nickname : username;
    }

    /** 소셜 제공자의 "기본 이미지"로 보이는 URL이면 true */
    private boolean isSocialDefaultImage(String imageUrl) {
        if (imageUrl == null) return true;
        String u = imageUrl.toLowerCase();
        // 프로젝트 상황에 맞춰 규칙을 더 넣어도 됨
        return u.contains("default")
                || u.contains("googleusercontent.com/a/")   // 구글 기본 아바타 패턴
                || u.endsWith("/s96-c")                     // 구글 썸네일 기본 사이즈 흔적
                || u.contains("kakao") && u.contains("default_") // 카카오 기본
                || u.contains("naver") && u.contains("default"); // 네이버 기본
    }
}
