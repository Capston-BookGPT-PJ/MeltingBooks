package com.example.meltingbooks.network.recommend;

import java.io.Serializable;

public class RecommendBookResponse implements Serializable {
    private int id;
    private String bookTitle;
    private String author;
    private String bookCoverUrl;
    private double hybridScore;
    private String createdAt;

    // Getters
    public int getId() { return id; }
    public String getBookTitle() { return bookTitle; }
    public String getAuthor() { return author; }
    public String getBookCoverUrl() { return bookCoverUrl; }
    public double getHybridScore() { return hybridScore; }
    public String getCreatedAt() { return createdAt; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    public void setAuthor(String author) { this.author = author; }
    public void setBookCoverUrl(String bookCoverUrl) { this.bookCoverUrl = bookCoverUrl; }
    public void setHybridScore(double hybridScore) { this.hybridScore = hybridScore; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
