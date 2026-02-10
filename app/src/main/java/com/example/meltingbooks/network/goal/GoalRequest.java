package com.example.meltingbooks.network.goal;

public class GoalRequest {
    private int userId;
    private String goalType; // "MONTHLY" or "YEARLY"
    private int targetBooks;
    private int targetReviews;
    private int targetMinutes;
    private String startDate;
    private String endDate;

    public GoalRequest() {
    }

    public GoalRequest(int userId, String goalType, int targetBooks, int targetReviews,
                       int targetMinutes, String startDate, String endDate) {
        this.userId = userId;
        this.goalType = goalType;
        this.targetBooks = targetBooks;
        this.targetReviews = targetReviews;
        this.targetMinutes = targetMinutes;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getGoalType() {
        return goalType;
    }

    public void setGoalType(String goalType) {
        this.goalType = goalType;
    }

    public int getTargetBooks() {
        return targetBooks;
    }

    public void setTargetBooks(int targetBooks) {
        this.targetBooks = targetBooks;
    }

    public int getTargetReviews() {
        return targetReviews;
    }

    public void setTargetReviews(int targetReviews) {
        this.targetReviews = targetReviews;
    }

    public int getTargetMinutes() {
        return targetMinutes;
    }

    public void setTargetMinutes(int targetMinutes) {
        this.targetMinutes = targetMinutes;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}
