package com.example.project.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter @Setter
public class CreateReviewRequest {
    private Long bookId;                 // 선택
    private String content;              // 필수
    private Integer rating;              // 선택 (null 허용)
    private List<String> hashtags;       // 선택 ["#소설", "추천"]
    private String imageUrl;             // (이전 호환) 단일 URL - 유지
    private List<String> imageUrls;      // 선택: 여러 URL
    private List<MultipartFile> files;   // 선택: 업로드 파일
}
