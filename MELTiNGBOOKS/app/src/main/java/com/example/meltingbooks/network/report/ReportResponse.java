package com.example.meltingbooks.network.report;

import java.util.List;

public class ReportResponse {
    private int totalPages;
    private int totalMinutes;
    private int completedReviews;
    private int averageDailyMinutes;
    private int experience;
    private int level;
    private List<Badge> badges;

    public static class Badge {
        private int id;
        private String badgeType;
        private String tier;
        private String imageUrl;
        private String createdAt;

        public String getBadgeType() { return badgeType; }
        public String getTier() { return tier; }
        public String getImageUrl() { return imageUrl; }
        public String getCreatedAt() { return createdAt; }
    }

    public int getTotalPages() { return totalPages; }
    public int getTotalMinutes() { return totalMinutes; }
    public int getCompletedReviews() { return completedReviews; }
    public int getAverageDailyMinutes() { return averageDailyMinutes; }
    public int getExperience() { return experience; }
    public int getLevel() { return level; }
    public List<Badge> getBadges() { return badges; }
}
