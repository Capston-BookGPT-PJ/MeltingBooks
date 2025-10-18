package com.example.project.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCommentRequest {
    private Long reviewId;
    private Long userId;
    private String content;
}
