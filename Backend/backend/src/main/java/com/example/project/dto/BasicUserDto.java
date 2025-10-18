package com.example.project.dto;

import com.example.project.entity.User;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class BasicUserDto {
    private Long id;
    private String nickname;
    private String profileImageUrl;
    private String bio;

    public static BasicUserDto from(User u) {
        return BasicUserDto.builder()
                .id(u.getId())
                .nickname(u.getNickname())
                .profileImageUrl(u.getProfileImageUrl())
                .bio(u.getBio())
                .build();
    }
}
