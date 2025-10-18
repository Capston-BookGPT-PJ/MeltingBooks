package com.example.project.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExternalBookDto {
    private String title;
    private String author;
    private String publisher;
    private String pubDate;
    private String isbn;
    private String isbn13;
    private String cover;
    private String link;
    private String categoryName;
    private Integer itemPage;
}
