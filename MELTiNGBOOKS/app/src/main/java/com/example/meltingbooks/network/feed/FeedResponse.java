package com.example.meltingbooks.network.feed;

import com.example.meltingbooks.network.book.Book;

import java.io.Serializable;
import java.util.List;

public class FeedResponse  implements Serializable {
    private int reviewId;
    private int userId;
    private String tagId;
    private String username;
    private String userProfileImage;
    private String content;
    private Integer rating;
    private List<String> reviewImageUrls;
    private String createdAt;
    private int likeCount;
    private int commentCount;
    private List<String> hashtags;
    private Integer bookId;
    private String bookTitle;
    private String nickname;
    private String shareUrl; //⭐추가



    // 추가 필드
    //private boolean liked;
    private Book book; // FeedDetailActivity에서 캐싱용


    // --- 댓글 관련 필드 추가 ---
    private int commentId;          // 댓글 고유 ID
    private String commentContent;  // 댓글 내용
    private String commentCreatedAt; // 댓글 작성 시간

    // ✅ 추가된 필드
    private boolean likedByMe;          // JSON의 likedByMe
    private List<String> likedUsers;    // JSON의 likedUsers


    // Getter
    public int getReviewId() { return reviewId; }
    public int getUserId() { return userId; }
    public String getTagId() { return tagId; }
    public String getUsername() { return username; }
    public String getUserProfileImage() { return userProfileImage; }
    public String getContent() { return content; }
    public Integer getRating() { return rating; }
    public List<String> getReviewImageUrls() { return reviewImageUrls; }
    public String getCreatedAt() { return createdAt; }
    public String getFormattedCreatedAt() {
        try {
            java.time.LocalDateTime dateTime =
                    java.time.LocalDateTime.parse(createdAt, java.time.format.DateTimeFormatter.ISO_DATE_TIME);
            return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } catch (Exception e) {
            return createdAt;
        }
    }
    public int getLikeCount() { return likeCount; }
    public int getCommentCount() { return commentCount; }
    public List<String> getHashtags() { return hashtags; }
    //해시태그 setter추가
    public void setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
    }
    public Integer getBookId() { return bookId; }
    public String getBookTitle() { return bookTitle; }

    public String getNickname() { return nickname; }

    // --- liked Getter/Setter ---
    /*public boolean isLiked() { return liked; }
    public void setLiked(boolean liked) { this.liked = liked; }*/

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }
    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    // --- Book Getter/Setter ---
    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }

    // --- 댓글 Getter/Setter ---
    public int getCommentId() { return commentId; }
    public void setCommentId(int commentId) { this.commentId = commentId; }

    public String getCommentContent() { return commentContent; }
    public void setCommentContent(String commentContent) { this.commentContent = commentContent; }

    public String getCommentCreatedAt() { return commentCreatedAt; }
    public void setCommentCreatedAt(String commentCreatedAt) { this.commentCreatedAt = commentCreatedAt; }



    //피드 갱신용
    public void setContent(String content) {
        this.content = content;
    }

    public void setReviewImageUrls(List<String> reviewImageUrls) {
        this.reviewImageUrls = reviewImageUrls;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setReviewId(int reviewId) {
        this.reviewId = reviewId;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }


    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setUserProfileImage(String userProfileImage) {
        this.userProfileImage = userProfileImage;
    }

    public String getShareUrl(){return shareUrl;} //⭐추가

    public void setShareUrl(String shareUrl) { //⭐추가
        this.shareUrl = shareUrl;
    }

    // ✅ likedByMe Getter/Setter
    public boolean isLikedByMe() { return likedByMe; }
    public void setLikedByMe(boolean likedByMe) { this.likedByMe = likedByMe; }

    // ✅ likedUsers Getter/Setter
    public List<String> getLikedUsers() { return likedUsers; }
    public void setLikedUsers(List<String> likedUsers) { this.likedUsers = likedUsers; }
}

