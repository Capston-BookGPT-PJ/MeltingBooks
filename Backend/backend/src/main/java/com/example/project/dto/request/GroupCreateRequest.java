package com.example.project.dto.request;

import lombok.Data;

@Data
public class GroupCreateRequest {
    private String name;
    private String description;
    private String groupImageUrl;
    private String category;
}