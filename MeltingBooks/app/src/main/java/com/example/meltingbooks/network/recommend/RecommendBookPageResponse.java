package com.example.meltingbooks.network.recommend;

import java.util.List;

public class RecommendBookPageResponse {
    private List<RecommendBookResponse> content;
    private boolean last;
    private int totalPages;

    public List<RecommendBookResponse> getContent() { return content; }
    public void setContent(List<RecommendBookResponse> content) { this.content = content; }

    public boolean isLast() { return last; }
    public void setLast(boolean last) { this.last = last; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
}
