package com.example.project.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateRequest {
    private String email;
    private String nickname;
    private String username;
    private String profileImageUrl;
    private String bio;
    private String tagId;
}
