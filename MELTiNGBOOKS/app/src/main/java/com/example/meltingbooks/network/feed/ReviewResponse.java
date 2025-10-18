package com.example.meltingbooks.network.feed;

import java.util.List;

public class ReviewResponse {
    private int reviewId;
    private String content;
    private List<String> reviewImageUrls;
    private Integer rating;
    private int userId;
    private Integer bookId;
    private String createdAt;
    private String updatedAt;
    //해시태그 추가
    private List<String> hashtags;

    // Getter & Setter
    public int getReviewId() {
        return reviewId;
    }

    public void setReviewId(int reviewId) {
        this.reviewId = reviewId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getReviewImageUrls() {
        return reviewImageUrls;
    }

    public void setReviewImageUrls(List<String> reviewImageUrls) {
        this.reviewImageUrls = reviewImageUrls;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
    //해시태그 추가
    public List<String> getHashtags() { return hashtags; }
}
