package com.example.meltingbooks.network.group.feed;

public class CreatePostRequest {
    // postType은 항상 REVIEW
    private final String postType = "REVIEW";
    private String title;
    private String content;

    // 기본 생성자
    public CreatePostRequest() {}

    // 전체 생성자
    public CreatePostRequest(String title, String content) {
        this.title = title;
        this.content = content;
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


}

