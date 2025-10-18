package com.example.meltingbooks.network.group.feed;

public class UpdatePostRequest {
    // postType은 항상 REVIEW로 고정
    private final String postType = "REVIEW";
    private String title;
    private String content;
    private String imageUrl;   // 이미지가 없으면 null

    // 기본 생성자
    public UpdatePostRequest() {}

    // 전체 생성자
    public UpdatePostRequest(String title, String content, String imageUrl) {
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
    }

    // Getter & Setter
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
