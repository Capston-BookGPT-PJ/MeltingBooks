package com.example.project.dto;

import com.example.project.entity.GroupPost;
import lombok.Data;

import java.util.List;

@Data
public class GroupPostCreateRequest {
    private GroupPost.PostType postType;   // REVIEW / NOTICE / GOAL_SHARE / RECOMMENDED_BOOK
    private String title;
    private String content;
    // 글 생성 시 외부 URL로 이미지를 받는 경우(선택)
    private List<String> imageUrls;
}
