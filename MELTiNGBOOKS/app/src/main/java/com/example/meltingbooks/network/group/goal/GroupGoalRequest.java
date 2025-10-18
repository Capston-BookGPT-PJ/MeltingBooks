package com.example.meltingbooks.network.group.goal;

public class GroupGoalRequest {
    private String title;
    private String description;
    private String startDate;
    private String endDate;
    private int targetBooks;
    private int targetReviews;
    private int targetMinutes;

    public GroupGoalRequest() {}

    public GroupGoalRequest(String title, String description, String startDate, String endDate,
                            int targetBooks, int targetReviews, int targetMinutes) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.targetBooks = targetBooks;
        this.targetReviews = targetReviews;
        this.targetMinutes = targetMinutes;
    }

    // --- Getters & Setters ---
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
}
