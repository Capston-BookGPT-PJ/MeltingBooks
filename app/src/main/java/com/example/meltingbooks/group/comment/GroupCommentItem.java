package com.example.meltingbooks.group.comment;

public class GroupCommentItem {
    private int Id;
    private int userId;
    private String userName;
    private String content;
    private String profileImageUrl;  // 이미지 리소스 ID
    private String commentDate;//작성 날짜


    // 서버 댓글용 생성자
    public GroupCommentItem(int Id, int userId, String userName, String content, String profileImageUrl, String commentDate) {
        this.Id = Id;
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.profileImageUrl = profileImageUrl;// 서버에서 URL로 가져오면 여기 바꿔도 됨
        this.commentDate = commentDate;
    }

    // Getter / Setter
    public int getCommentId() { return Id; }
    public void setCommentId(int Id) { this.Id = Id; }

    public int getUserId() { return userId; }
    public String getUserName() {
        return userName;
    }

    public String getContent() {
        return content;
    }

    public String getCommentDate() { return commentDate; }

    public String getProfileImageUrl() { return profileImageUrl; }
}
