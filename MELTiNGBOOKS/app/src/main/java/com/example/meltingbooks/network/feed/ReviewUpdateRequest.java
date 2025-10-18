package com.example.meltingbooks.network.feed;

import java.util.List;

public class ReviewUpdateRequest {
    private String content;
    private String imageUrl;
    private Integer bookId;
    private Integer rating;
    private List<String> hashtags;

    public ReviewUpdateRequest(String content, String imageUrl) {
        this.content = content;
        this.imageUrl = imageUrl;
    }


    public ReviewUpdateRequest(String content, String imageUrl, Integer bookId, Integer rating, List<String> hashtags) {
        this.content = content;
        this.imageUrl = imageUrl;
        this.bookId = bookId;
        this.rating = rating;
        this.hashtags = hashtags;

    }


    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public List<String> getHashtags() {
        return hashtags;
    }

    public void setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
    }
}

