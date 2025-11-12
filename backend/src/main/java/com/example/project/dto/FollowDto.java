package com.example.project.dto;

import com.example.project.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FollowDto {
    private Long id;
    private String nickname;
    private String profileImageUrl;

    public static FollowDto from(User user) {
        return new FollowDto(
                user.getId(),
                user.getNickname(),
                user.getProfileImageUrl()
        );
    }
}
