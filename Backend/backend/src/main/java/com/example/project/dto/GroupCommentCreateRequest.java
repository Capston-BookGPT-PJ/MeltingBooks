package com.example.project.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupCommentCreateRequest {
    private String content;
}