package com.example.meltingbooks.network.group.comment;

import java.util.List;

public class GroupCommentPageResponse {
    private List<GroupCommentResponse> content;
    // 필요하면 pageable, totalElements 등 추가

    public List<GroupCommentResponse> getContent() {
        return content;
    }

    public void setContent(List<GroupCommentResponse> content) {
        this.content = content;
    }
}
