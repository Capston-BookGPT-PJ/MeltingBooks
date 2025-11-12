package com.example.meltingbooks.network.feed;

public class CommentRequest {
    private String content;

    public CommentRequest(String content) {
        this.content = content;
    }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
