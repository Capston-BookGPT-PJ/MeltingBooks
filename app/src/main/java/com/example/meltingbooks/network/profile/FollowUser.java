package com.example.meltingbooks.network.profile;

public class FollowUser {
    private int id;
    private String nickname;
    private String profileImageUrl;

    // getter & setter
    public int getId() { return id; }
    public String getNickname() { return nickname; }
    public String getProfileImageUrl() { return profileImageUrl; }

    public void setId(int id) { this.id = id; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
}