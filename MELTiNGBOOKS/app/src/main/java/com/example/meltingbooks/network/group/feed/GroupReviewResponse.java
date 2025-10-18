package com.example.meltingbooks.network.group.feed;

import java.io.Serializable;
import java.util.List;

public class GroupReviewResponse implements Serializable {
    private int id; //postId
    private int groupId;
    private int userId;
    private String postType;
    private String title;
    private String content;
    private List<String> imageUrls;
    private String createdAt;


    // --- Getter ---
    public int getId() {
        return id;
    }

    public int getGroupId() {
        return groupId;
    }

    public int getUserId() {
        return userId;
    }

    public String getPostType() {
        return postType;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}
