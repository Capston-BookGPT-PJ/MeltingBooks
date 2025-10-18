package com.example.project.dto;

import com.example.project.entity.ReadingLog;
import com.example.project.entity.User;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String email;
    private String nickname;
    private String username;
    private String profileImageUrl;
    private String bio;
    private String tagId;

    private int level;
    private int experience;
    private List<BadgeDto> badges;

    private int followerCount;
    private int followingCount;
    private int reviewCount;

    // 최근 읽은 책 3권 / 최근 작성 리뷰 3개
    private List<BookDto> recentBooks;
    private List<ReviewDto> recentReviews;

    // 사용자가 속한 그룹 이름 (최대 3개)
    private List<String> groupNames;

    /** 그룹 이름을 주입하지 않을 때 기본 변환 */
    public static UserDto from(User user) {
        return from(user, List.of());
    }

    /** Service가 조회한 groupNames(최대 3개)를 주입하는 변환 */
    public static UserDto from(User user, List<String> groupNames) {
        List<BadgeDto> badgeDtos = user.getBadges() == null ? List.of() :
                user.getBadges().stream()
                        .map(BadgeDto::from)
                        .collect(Collectors.toList());

        List<ReviewDto> recentReviews = user.getReviews() == null ? List.of() :
                user.getReviews().stream()
                        .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                        .limit(3)
                        .map(ReviewDto::from)
                        .collect(Collectors.toList());

        List<BookDto> recentBooks = user.getReadingLogs() == null ? List.of() :
                user.getReadingLogs().stream()
                        .sorted((l1, l2) -> l2.getReadAt().compareTo(l1.getReadAt()))
                        .map(ReadingLog::getBook)
                        .distinct()
                        .limit(3)
                        .map(BookDto::from)
                        .collect(Collectors.toList());

        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .username(user.getUsername())
                .profileImageUrl(user.getProfileImageUrl())
                .bio(user.getBio())
                .tagId(user.getTagId())
                .level(user.getLevel())
                .experience(user.getExperience())
                .badges(badgeDtos)
                .followerCount(user.getFollowers() == null ? 0 : user.getFollowers().size())
                .followingCount(user.getFollowing() == null ? 0 : user.getFollowing().size())
                .reviewCount(user.getReviews() == null ? 0 : user.getReviews().size())
                .recentBooks(recentBooks)
                .recentReviews(recentReviews)
                .groupNames(groupNames == null ? List.of() : groupNames)
                .build();
    }
}
