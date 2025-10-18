package com.example.meltingbooks.group.menu;

public class GroupMemberItem {
    private int groupId;
    private int userId;
    private String nickname;
    private String username;
    private String profileImageUrl;
    private String joinStatus;
    private String joinedAt;

    // 생성자
    public GroupMemberItem(int userId, String nickname, String username, String profileImageUrl, String joinStatus, String joinedAt) {
        this.userId = userId;
        this.nickname = nickname;
        this.username = username;
        this.profileImageUrl = profileImageUrl;
        this.joinStatus = joinStatus;
        this.joinedAt = joinedAt;
    }

    // Getter

    public int getGroupId() { return groupId; }

    public int getUserId() {
        return userId;
    }

    public String getNickname() {
        return nickname;
    }

    public String getUsername() {
        return username;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getJoinStatus() {
        return joinStatus;
    }

    public String getJoinedAt() {
        return joinedAt;
    }

    // Setter (필요시)
    public void setGroupId(int groupId) { this.groupId = groupId; }
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void setJoinStatus(String joinStatus) {
        this.joinStatus = joinStatus;
    }

    public void setJoinedAt(String joinedAt) {
        this.joinedAt = joinedAt;
    }
}
