// 유저 정보 네트워크 응답 전용 DTO
package com.example.meltingbooks.network.profile;

import java.util.List;

public class UserResponse {
    private int id;
    private String email;
    private String nickname;
    private String username;
    private String profileImageUrl;
    private String bio;
    private String tagId;
    private int level;
    private int experience;
    private List<Badge> badges;
    private int followerCount;
    private int followingCount;
    private int reviewCount;
    private List<Book> recentBooks;
    private List<Review> recentReviews;

    // ----------------- Nested Models -----------------
    public static class Badge {
        private int id;
        private String badgeType;
        private String tier;
        private String createdAt;
        private String imageUrl;    // 이미지 URL

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getBadgeType() { return badgeType; }
        public void setBadgeType(String badgeType) { this.badgeType = badgeType; }

        public String getTier() { return tier; }
        public void setTier(String tier) { this.tier = tier; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

        public String getImageUrl() { return imageUrl; }
    }

    public static class Book {
        private String title;
        private String author;
        private String publisher;
        private String pubDate;
        private String isbn;
        private String isbn13;
        private String cover;
        private String link;
        private String categoryName;
        private int itemPage;
        private int popularityScore;

        // Getter & Setter
        public String getTitle() {
            return title;
        }
        public void setTitle(String title) {
            this.title = title;
        }

        public String getAuthor() {
            return author;
        }
        public void setAuthor(String author) {
            this.author = author;
        }

        public String getPublisher() {
            return publisher;
        }
        public void setPublisher(String publisher) {
            this.publisher = publisher;
        }

        public String getPubDate() {
            return pubDate;
        }
        public void setPubDate(String pubDate) {
            this.pubDate = pubDate;
        }

        public String getIsbn() {
            return isbn;
        }
        public void setIsbn(String isbn) {
            this.isbn = isbn;
        }

        public String getIsbn13() {
            return isbn13;
        }
        public void setIsbn13(String isbn13) {
            this.isbn13 = isbn13;
        }

        public String getCover() {
            return cover;
        }
        public void setCover(String cover) {
            this.cover = cover;
        }

        public String getLink() {
            return link;
        }
        public void setLink(String link) {
            this.link = link;
        }

        public String getCategoryName() {
            return categoryName;
        }
        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public int getItemPage() {
            return itemPage;
        }
        public void setItemPage(int itemPage) {
            this.itemPage = itemPage;
        }

        public int getPopularityScore() {
            return popularityScore;
        }
        public void setPopularityScore(int popularityScore) {
            this.popularityScore = popularityScore;
        }

    }

    public static class Review {
        private int reviewId;
        private String content;
        private String imageUrl;
        private  int rating;
        private  int userId;
        private  Integer bookId;
        private String createdAt;


        // Getter & Setter
        public int getReviewId() {
            return reviewId;
        }
        public void setReviewId(int reviewId) {
            this.reviewId = reviewId;
        }

        public String getContent() {
            return content;
        }
        public void setContent(String content) {
            this.content = content;
        }

        public String getImageUrl() {
            return imageUrl;
        }
        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public int getRating() {
            return rating;
        }
        public void setRating(int rating) {
            this.rating = rating;
        }

        public int getUserId() {
            return userId;
        }
        public void setUserId(int userId) {
            this.userId = userId;
        }

        public Integer getBookId() {
            return bookId;
        }
        public void setBookId(Integer bookId) {
            this.bookId = bookId;
        }

        public String getCreatedAt(){return createdAt;}

        public String getFormattedCreatedAt() {
            try {
                java.time.LocalDateTime dateTime =
                        java.time.LocalDateTime.parse(createdAt, java.time.format.DateTimeFormatter.ISO_DATE_TIME);
                return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            } catch (Exception e) {
                return createdAt;
            }
        }
        public void setCreatedAt(String createdAt){
            this.createdAt = createdAt;
        }

    }

    // ----------------- Getter & Setter -----------------
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getTagId() { return tagId; }
    public void setTagId(String tagId) { this.tagId = tagId; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }

    public List<Badge> getBadges() { return badges; }
    public void setBadges(List<Badge> badges) { this.badges = badges; }

    public int getFollowerCount() { return followerCount; }
    public void setFollowerCount(int followerCount) { this.followerCount = followerCount; }

    public int getFollowingCount() { return followingCount; }
    public void setFollowingCount(int followingCount) { this.followingCount = followingCount; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

    public List<Book> getRecentBooks() { return recentBooks; }
    public void setRecentBooks(List<Book> recentBooks) { this.recentBooks = recentBooks; }

    public List<Review> getRecentReviews() { return recentReviews; }
    public void setRecentReviews(List<Review> recentReviews) { this.recentReviews = recentReviews; }
}
