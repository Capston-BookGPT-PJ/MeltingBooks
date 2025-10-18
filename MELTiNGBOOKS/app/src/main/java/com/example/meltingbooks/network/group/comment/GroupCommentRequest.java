package com.example.meltingbooks.network.group.comment;

public class GroupCommentRequest {
    private String content;

    public GroupCommentRequest(String content) {
        this.content = content;
    }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
