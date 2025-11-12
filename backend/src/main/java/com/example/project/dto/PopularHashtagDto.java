package com.example.project.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PopularHashtagDto {
    private String tag;
    private Long count;
}
