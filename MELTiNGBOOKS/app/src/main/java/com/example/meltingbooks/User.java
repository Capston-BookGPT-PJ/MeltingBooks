package com.example.meltingbooks;

public class User {
    private String name;
    private String intro;
    private int imageResId;

    public User(String name, String intro, int imageResId) {
        this.name = name;
        this.intro = intro;
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public String getIntro() {
        return intro;
    }
    public int getImageResId() {
        return imageResId;
    }
}