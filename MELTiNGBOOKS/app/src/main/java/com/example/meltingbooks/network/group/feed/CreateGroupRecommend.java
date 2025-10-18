package com.example.meltingbooks.network.group.feed;

import java.util.List;

public class CreateGroupRecommend {
    private final String postType = "RECOMMENDED_BOOK"; // 추천도서용 고정
    private String title;
    private String content;
    private List<String> imageUrls; // 이미지 URL 리스트 추가

    public CreateGroupRecommend(String title, String content, List<String> imageUrls) {
        this.title = title;
        this.content = content;
        this.imageUrls = imageUrls;
    }

    public String getPostType() {
        return postType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
}
