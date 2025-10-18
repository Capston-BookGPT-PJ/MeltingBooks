package com.example.meltingbooks.network.group.comment;

public class GroupCommentResponse {
    private int id;                     // 댓글 ID
    private int postId;                 // 댓글이 속한 포스트 ID
    private int userId;                 // 작성자 ID
    private String nickname;            // 작성자 닉네임
    private String profileImageUrl;     // 작성자 프로필 이미지, null 가능
    private String content;             // 댓글 내용
    private String createdAt;           // 생성일시 ISO 포맷
    private String tagId;              // null 가능


    public GroupCommentResponse() {}

    // Getter & Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    // 편의 메서드: 보기 좋은 날짜 형식 반환
    public String getFormattedCreatedAt() {
        try {
            java.time.LocalDateTime dateTime =
                    java.time.LocalDateTime.parse(createdAt, java.time.format.DateTimeFormatter.ISO_DATE_TIME);
            return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } catch (Exception e) {
            return createdAt;
        }
    }
}
