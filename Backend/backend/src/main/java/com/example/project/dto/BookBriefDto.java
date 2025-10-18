package com.example.project.dto;

import com.example.project.entity.Book;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BookBriefDto {
    private Long id;
    private String title;
    private String coverUrl;

    public static BookBriefDto from(Book b) {
        if (b == null) return null;
        return BookBriefDto.builder()
                .id(b.getId())
                .title(b.getTitle())
                .coverUrl(b.getCover())
                .build();
    }
}
