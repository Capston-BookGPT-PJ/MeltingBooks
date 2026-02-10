package com.example.meltingbooks.network.feed;

import java.util.List;

public class UserReviewResponse {
    private int reviewId;
    private String content;
    private String imageUrl;
    private Integer rating;
    private int userId;
    private Integer bookId;
    private List<String> hashtags;

    //getter
    public int getReviewId() { return reviewId; }
    public String getContent() { return content; }
    public String getImageUrl() { return imageUrl; }
    public Integer getRating() { return rating; }
    public int getUserId() { return userId; }
    public Integer getBookId() { return bookId; }
    public List<String> getHashtags() { return hashtags; }
}