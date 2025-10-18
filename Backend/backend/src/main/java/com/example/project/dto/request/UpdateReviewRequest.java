package com.example.project.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter @Setter
public class UpdateReviewRequest {
    private String content;              // 선택(부분 업데이트 허용)
    private Integer rating;              // 선택
    private List<String> hashtags;       // 선택 (전체 교체)
    private List<String> imageUrls;      // 선택 (전체 교체)
    private List<MultipartFile> files;   // 선택 (추가 업로드)
    private Boolean clearImages;         // 선택: true이면 기존 이미지 전부 삭제
    private Long bookId;
}
