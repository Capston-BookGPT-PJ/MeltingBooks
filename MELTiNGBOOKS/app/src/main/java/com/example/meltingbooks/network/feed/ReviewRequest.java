package com.example.meltingbooks.network.feed;

import java.util.List;

// 리뷰 작성 요청 DTO
public class ReviewRequest {
        private Integer bookId;
        private String content;
        private Integer rating;
        private List<String> hashtags;

    // 기존 생성자 유지 (호환성 위해)
    public ReviewRequest(Integer bookId, String content, Integer rating) {
        this.bookId = bookId;
        this.content = content;
        this.rating = rating;
    }

    //해시태그 추가시 생성자
    public ReviewRequest(Integer bookId, String content, Integer rating, List<String> hashtags) {
        this.bookId = bookId;
        this.content = content;
        this.rating = rating;
        this.hashtags = hashtags;
    }



    public Integer getBookId() { return bookId; }
    public void setBookId(Integer bookId) { this.bookId = bookId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    //해시태그 추가
    public List<String> getHashtags() { return hashtags; }
    public void setHashtags(List<String> hashtags) { this.hashtags = hashtags; }

}