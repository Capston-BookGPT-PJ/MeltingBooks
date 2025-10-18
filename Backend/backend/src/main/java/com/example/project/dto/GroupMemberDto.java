package com.example.project.dto;

import com.example.project.entity.GroupMember;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMemberDto {

    private Long userId;
    private String nickname;
    private String username;
    private String profileImageUrl;

    private GroupMember.JoinStatus joinStatus;
    private LocalDateTime joinedAt;

    public static GroupMemberDto from(GroupMember m) {
        return GroupMemberDto.builder()
                .userId(m.getUser().getId())
                .nickname(m.getUser().getNickname())
                .username(m.getUser().getUsername())
                .profileImageUrl(m.getUser().getProfileImageUrl())
                .joinStatus(m.getJoinStatus())
                .joinedAt(m.getJoinedAt())
                .build();
    }
}
