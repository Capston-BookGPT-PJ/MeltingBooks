package com.example.project.dto;

import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupFeedSectionResponse {
    private List<FeedDto> notices;
    private List<FeedDto> recommendedBooks;
    private List<GroupGoalCardDto> goals;
    private Page<FeedDto> posts;
}
