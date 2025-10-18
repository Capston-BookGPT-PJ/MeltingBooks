package com.example.meltingbooks.group;

import java.io.Serializable;
import java.util.List;

public class GroupFeedItem implements Serializable {
    private int postId;          // id / reviewId
    private int groupId;         // groupId
    private int userId;          // userId (기존 authorId)
    private String userName;     // username (기존 authorName)
    // postType은 항상 REVIEW로 고정
    private String postType;
    private String title;        // title
    private String content;      // content
    private List<String> imageUrls;     // imageUrls[0] / reviewImageUrls[0]
    private String createdAt;    // createdAt
    private int commentCount;    // commentCount
    private int likeCount;       // likeCount
    private boolean likedByMe;   // likedByMe / liked
    private List<String> likedUsers; // likedUsers
    private String userProfileImage; // userProfileImage (기존 profileImageUrl)
    private String tagId;        // tagId



    public GroupFeedItem(String postType, String userName, String title,String content, String createdAt,
                         List<String> imageUrls, String userProfileImage, int commentCount, int likeCount, String tagId, int groupId, int userId) {
        this.postType = postType;
        this.userName = userName;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.imageUrls = imageUrls;
        this.userProfileImage = userProfileImage;
        this.commentCount = commentCount;
        this.likeCount = likeCount;
        this.tagId = tagId;
        this.groupId = groupId;
        this.userId = userId;
    }

    public GroupFeedItem(String postType, String title, String content, List<String> imageUrls, int groupId) {
        this.postType = postType;
        this.title = title;
        this.content = content;
        this.imageUrls = imageUrls;
        this.groupId = groupId;
    }

    // --- Getter ---
    public int getPostId() {
        return postId;
    }

    public int getGroupId() {
        return groupId;
    }

    public int getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getPostType() {
        return postType;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    // getter / setter
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    // 기존 getImageUrl() 대신 첫 번째 이미지를 바로 가져오는 편의 메서드
    public String getFirstImageUrl() {
        return (imageUrls != null && !imageUrls.isEmpty()) ? imageUrls.get(0) : null;
    }


    public String getCreatedAt() {
        return createdAt;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public boolean isLikedByMe() { return likedByMe; }
    public List<String> getLikedUsers() { return likedUsers; }

    public String getUserProfileImage() {
        return userProfileImage;
    }

    public String getTagId() {
        return tagId;
    }

    // --- Setter ---
    // --- Setter ---
    public void setLikedByMe(boolean likedByMe) { this.likedByMe = likedByMe; }
    public void setLikedUsers(List<String> likedUsers) { this.likedUsers = likedUsers; }
    public void setPostId(int postId) {
        this.postId = postId;
    }
    public void setPostType(String postType) {this.postType = postType;}

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setUserName(String username) {
        this.userName = username;
    }


    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public void setUserProfileImage(String userProfileImage) {
        this.userProfileImage = userProfileImage;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

}
