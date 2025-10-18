package com.example.meltingbooks.network.profile;

public class BadgeCatalogResponse {
    private String badgeType;   // 예: FULL_READ
    private boolean obtained;   // 획득 여부
    private String tier;        // BRONZE, SILVER 등
    private String imageUrl;    // 이미지 URL

    public String getBadgeType() { return badgeType; }
    public boolean isObtained() { return obtained; }
    public String getTier() { return tier; }
    public String getImageUrl() { return imageUrl; }
}