package com.example.meltingbooks.network.recommend;

public class GoalRecommendResponse {
    private int recommendedBooks;
    private int recommendedMinutes;
    private int recommendedReviews;
    private String preferredPeriod;
    private int preferredHour;
    private int sessionMinutes;
    private int daysPerWeek;
    private int recommendedWeeklyMinutes;
    private String rationale;
    private int daysSinceLastRead;
    private boolean inactiveFlag;
    private String createdAt;

    // Getter
    public int getRecommendedBooks() { return recommendedBooks; }
    public int getRecommendedMinutes() { return recommendedMinutes; }
    public int getRecommendedReviews() { return recommendedReviews; }
    public String getPreferredPeriod() { return preferredPeriod; }
    public int getPreferredHour() { return preferredHour; }
    public int getSessionMinutes() { return sessionMinutes; }
    public int getDaysPerWeek() { return daysPerWeek; }
    public int getRecommendedWeeklyMinutes() { return recommendedWeeklyMinutes; }
    public String getRationale() { return rationale; }
    public int getDaysSinceLastRead() { return daysSinceLastRead; }
    public boolean isInactiveFlag() { return inactiveFlag; }
    public String getCreatedAt() { return createdAt; }
}
