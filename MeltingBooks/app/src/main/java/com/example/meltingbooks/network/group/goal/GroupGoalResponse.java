package com.example.meltingbooks.network.group.goal;

import java.util.List;

public class GroupGoalResponse {
    private int id;
    private int groupId;
    private String title;
    private String description;
    private String startDate;
    private String endDate;
    private int targetBooks;
    private int targetReviews;
    private int targetMinutes;
    private int completedBooks;
    private int completedReviews;
    private int completedMinutes;
    private String status;

    public GroupGoalResponse() {}

    public GroupGoalResponse(int id, int groupId, String title, String description,
                             String startDate, String endDate,
                             int targetBooks, int targetReviews, int targetMinutes,
                             int completedBooks, int completedReviews, int completedMinutes,
                             String status) {
        this.id = id;
        this.groupId = groupId;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.targetBooks = targetBooks;
        this.targetReviews = targetReviews;
        this.targetMinutes = targetMinutes;
        this.completedBooks = completedBooks;
        this.completedReviews = completedReviews;
        this.completedMinutes = completedMinutes;
        this.status = status;
    }

    // --- Getters & Setters ---
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

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

    public int getCompletedBooks() {
        return completedBooks;
    }

    public void setCompletedBooks(int completedBooks) {
        this.completedBooks = completedBooks;
    }

    public int getCompletedReviews() {
        return completedReviews;
    }

    public void setCompletedReviews(int completedReviews) {
        this.completedReviews = completedReviews;
    }

    public int getCompletedMinutes() {
        return completedMinutes;
    }

    public void setCompletedMinutes(int completedMinutes) {
        this.completedMinutes = completedMinutes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
