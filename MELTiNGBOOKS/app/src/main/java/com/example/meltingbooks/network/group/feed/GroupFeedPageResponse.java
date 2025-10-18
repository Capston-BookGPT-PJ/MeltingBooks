package com.example.meltingbooks.network.group.feed;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class GroupFeedPageResponse implements Serializable {

    @SerializedName("notices")
    private List<GroupFeedResponse.Post> notices;

    @SerializedName("recommendedBooks")
    private List<GroupFeedResponse.Post> recommendedBooks;

    @SerializedName("goals")
    private List<GroupFeedResponse.Post> goals;

    @SerializedName("posts")
    private PostsWrapper posts;

    // ✅ Getter
    public List<GroupFeedResponse.Post> getNotices() { return notices; }
    public List<GroupFeedResponse.Post> getRecommendedBooks() { return recommendedBooks; }
    public List<GroupFeedResponse.Post> getGoals() { return goals; }
    public PostsWrapper getPosts() { return posts; }

    // ✅ 내부 클래스 - posts 페이징 데이터
    public static class PostsWrapper implements Serializable {

        @SerializedName("content")
        private List<GroupFeedResponse.Post> content;

        @SerializedName("totalPages")
        private int totalPages;

        @SerializedName("totalElements")
        private int totalElements;

        @SerializedName("last")
        private boolean last;

        @SerializedName("size")
        private int size;

        @SerializedName("number")
        private int number;

        @SerializedName("numberOfElements")
        private int numberOfElements;

        @SerializedName("first")
        private boolean first;

        @SerializedName("empty")
        private boolean empty;

        // ✅ Getter
        public List<GroupFeedResponse.Post> getContent() { return content; }
        public int getTotalPages() { return totalPages; }
        public int getTotalElements() { return totalElements; }
        public boolean isLast() { return last; }
        public int getSize() { return size; }
        public int getNumber() { return number; }
        public int getNumberOfElements() { return numberOfElements; }
        public boolean isFirst() { return first; }
        public boolean isEmpty() { return empty; }
    }
}
