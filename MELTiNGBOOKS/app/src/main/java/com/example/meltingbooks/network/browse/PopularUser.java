package com.example.meltingbooks.network.browse;

import com.google.gson.annotations.SerializedName;

public class PopularUser {
    private int id;
    private String nickname;

    @SerializedName("profileImageUrl")
    private String profileImageUrl;

    private String bio;

    // Getter & Setter
    public int getId() { return id; }
    public String getNickname() { return nickname; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public String getBio() { return bio; }

    public void setId(int id) { this.id = id; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    public void setBio(String bio) { this.bio = bio; }
}