package com.example.project.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
@Builder
public class AuthResponse {
    private final UserDto user;
    private final String token;
    private final String refreshToken;
}
