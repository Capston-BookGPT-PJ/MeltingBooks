package com.example.project.dto;

import com.example.project.entity.GroupPost;
import lombok.Data;
import java.util.List;

@Data
public class GroupPostUpdateRequest {
    private GroupPost.PostType postType;  // 선택
    private String title;                 // 선택
    private String content;               // 선택
    private List<String> imageUrls;
}
