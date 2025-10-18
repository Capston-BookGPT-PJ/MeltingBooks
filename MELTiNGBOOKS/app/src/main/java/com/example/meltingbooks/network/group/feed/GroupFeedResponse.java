package com.example.meltingbooks.network.group.feed;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class GroupFeedResponse implements Serializable {

    private List<Post> notices;
    private List<Post> recommendedBooks;
    private List<Post> goals;
    private PostsWrapper<Post> posts;

    // ✅ Getter 수정
    public List<Post> getNotices() { return notices; }
    public List<Post> getRecommendedBooks() { return recommendedBooks; }
    public List<Post> getGoals() { return goals; }
    public PostsWrapper<Post> getPosts() { return posts; }

    // ✅ PostsWrapper 제네릭 구조 유지
    public static class PostsWrapper<T> implements Serializable {
        private List<T> content;
        public List<T> getContent() { return content; }
    }

    // ✅ Post 클래스는 서버 응답(JSON)에 맞춘 형태로 (reviewId, userId 등)
    public static class Post implements Serializable {
        private int reviewId;
        private int userId;
        private String tagId;
        private String nickname;
        private String username;
        private String userProfileImage;
        private String content;
        private int rating;

        @SerializedName("imageUrls")  // 서버 필드 이름
        private List<String> reviewImageUrls;
        private String createdAt;
        private int likeCount;
        private int commentCount;
        private List<String> hashtags;
        private boolean likedByMe;
        private List<String> likedUsers;
        private Integer bookId;
        private String bookTitle;
        private String shareUrl;


        //추가
        // Post 클래스
        private String title;
        private String postType;
        // --- Getter ---
        public int getReviewId() { return reviewId; }
        public int getUserId() { return userId; }
        public String getTagId() { return tagId; }
        public String getNickname() { return nickname; }
        public String getUsername() { return username; }
        public String getUserProfileImage() { return userProfileImage; }
        public String getContent() { return content; }
        public int getRating() { return rating; }
        public List<String> getReviewImageUrls() { return reviewImageUrls; }
        public String getCreatedAt() { return createdAt; }
        public int getLikeCount() { return likeCount; }
        public int getCommentCount() { return commentCount; }
        public List<String> getHashtags() { return hashtags; }
        public boolean isLikedByMe() { return likedByMe; }
        public List<String> getLikedUsers() { return likedUsers; }
        public Integer getBookId() { return bookId; }
        public String getBookTitle() { return bookTitle; }
        public String getShareUrl() { return shareUrl; }

        // --- Setter ---
        public void setReviewId(int reviewId) { this.reviewId = reviewId; }
        public void setUserId(int userId) { this.userId = userId; }
        public void setTagId(String tagId) { this.tagId = tagId; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        public void setUsername(String username) { this.username = username; }
        public void setUserProfileImage(String userProfileImage) { this.userProfileImage = userProfileImage; }
        public void setContent(String content) { this.content = content; }
        public void setRating(int rating) { this.rating = rating; }
        public void setReviewImageUrls(List<String> reviewImageUrls) { this.reviewImageUrls = reviewImageUrls; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
        public void setCommentCount(int commentCount) { this.commentCount = commentCount; }
        public void setHashtags(List<String> hashtags) { this.hashtags = hashtags; }
        public void setLikedByMe(boolean likedByMe) { this.likedByMe = likedByMe; }
        public void setLikedUsers(List<String> likedUsers) { this.likedUsers = likedUsers; }
        public void setBookId(Integer bookId) { this.bookId = bookId; }
        public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
        public void setShareUrl(String shareUrl) { this.shareUrl = shareUrl; }

        //// getter/setter -get은 null값
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getPostType() { return postType; }
        public void setPostType(String postType) { this.postType = postType; }
    }

}
