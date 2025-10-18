package com.example.meltingbooks.network.group.feed;

import java.util.List;

public class CreateGroupNotice {
    private final String postType = "NOTICE"; // 공지용 고정
    private String title;
    private String content;
    private List<String> imageUrls; // 이미지 URL 리스트 추가

    public CreateGroupNotice(String title, String content, List<String> imageUrls) {
        this.title = title;
        this.content = content;
        this.imageUrls = imageUrls;
    }
    public CreateGroupNotice() { }

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