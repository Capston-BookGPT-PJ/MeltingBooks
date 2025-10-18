package com.example.project.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyGroupDto {
    private Long groupId;
    private String name;
    private String description;
    private String category;
    private int memberCount;
}
