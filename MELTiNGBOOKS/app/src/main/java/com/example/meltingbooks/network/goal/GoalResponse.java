package com.example.meltingbooks.network.goal;

import java.util.List;

public class GoalResponse {
    private int id;
    private int userId;
    private String goalType;
    private int targetBooks;
    private int completedBooks;
    private int targetReviews;
    private int completedReviews;
    private int targetMinutes;
    private int completedMinutes;
    private float bookProgress;
    private float reviewProgress;
    private float timeProgress;
    private String startDate;
    private String endDate;
    private int year;
    private Integer month;
    private String booksProgressText;
    private String reviewsProgressText;
    private String timeProgressText;

    private List<BookInfo> books; // 추후 확장

    public GoalResponse() {
    }

    public GoalResponse(int id, int userId, String goalType, int targetBooks, int completedBooks,
                        int targetReviews, int completedReviews, int targetMinutes, int completedMinutes,
                        float bookProgress, float reviewProgress, float timeProgress, String startDate,
                        String endDate, int year, Integer month, String booksProgressText,
                        String reviewsProgressText, String timeProgressText) { //, List<BookInfo> books 추후 추가
        this.id = id;
        this.userId = userId;
        this.goalType = goalType;
        this.targetBooks = targetBooks;
        this.completedBooks = completedBooks;
        this.targetReviews = targetReviews;
        this.completedReviews = completedReviews;
        this.targetMinutes = targetMinutes;
        this.completedMinutes = completedMinutes;
        this.bookProgress = bookProgress;
        this.reviewProgress = reviewProgress;
        this.timeProgress = timeProgress;
        this.startDate = startDate;
        this.endDate = endDate;
        this.year = year;
        this.month = month;
        this.booksProgressText = booksProgressText;
        this.reviewsProgressText = reviewsProgressText;
        this.timeProgressText = timeProgressText;
        //this.books = books;
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public int getCompletedBooks() {
        return completedBooks;
    }

    public void setCompletedBooks(int completedBooks) {
        this.completedBooks = completedBooks;
    }

    public int getTargetReviews() {
        return targetReviews;
    }

    public void setTargetReviews(int targetReviews) {
        this.targetReviews = targetReviews;
    }

    public int getCompletedReviews() {
        return completedReviews;
    }

    public void setCompletedReviews(int completedReviews) {
        this.completedReviews = completedReviews;
    }

    public int getTargetMinutes() {
        return targetMinutes;
    }

    public void setTargetMinutes(int targetMinutes) {
        this.targetMinutes = targetMinutes;
    }

    public int getCompletedMinutes() {
        return completedMinutes;
    }

    public void setCompletedMinutes(int completedMinutes) {
        this.completedMinutes = completedMinutes;
    }

    public float getBookProgress() {
        return bookProgress;
    }

    public void setBookProgress(float bookProgress) {
        this.bookProgress = bookProgress;
    }

    public float getReviewProgress() {
        return reviewProgress;
    }

    public void setReviewProgress(float reviewProgress) {
        this.reviewProgress = reviewProgress;
    }

    public float getTimeProgress() {
        return timeProgress;
    }

    public void setTimeProgress(float timeProgress) {
        this.timeProgress = timeProgress;
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

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public String getBooksProgressText() {
        return booksProgressText;
    }

    public void setBooksProgressText(String booksProgressText) {
        this.booksProgressText = booksProgressText;
    }

    public String getReviewsProgressText() {
        return reviewsProgressText;
    }

    public void setReviewsProgressText(String reviewsProgressText) {
        this.reviewsProgressText = reviewsProgressText;
    }

    public String getTimeProgressText() {
        return timeProgressText;
    }

    public void setTimeProgressText(String timeProgressText) {
        this.timeProgressText = timeProgressText;
    }

    public List<BookInfo> getBooks() {
        return books;
    }

    public void setBooks(List<BookInfo> books) {
        this.books = books;
    }

    // Inner class
    public static class BookInfo {
        private int id;
        private String title;
        private String coverUrl;

        public BookInfo() {
        }

        public BookInfo(int id, String title, String coverUrl) {
            this.id = id;
            this.title = title;
            this.coverUrl = coverUrl;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getCoverUrl() {
            return coverUrl;
        }

        public void setCoverUrl(String coverUrl) {
            this.coverUrl = coverUrl;
        }
    }
}
