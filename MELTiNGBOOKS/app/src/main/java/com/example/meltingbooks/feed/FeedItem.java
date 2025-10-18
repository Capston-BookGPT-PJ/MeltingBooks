package com.example.meltingbooks.feed;

import com.example.meltingbooks.network.book.Book;

import java.io.Serializable;
import java.util.List;

public class FeedItem implements Serializable {
    private int userId; // 글 작성자 ID
    private String userName;//유저 이름 닉네임!!
    private String tagId;
    private String reviewContent;//감상문
    private String reviewDate;//작성 날짜
    //private boolean liked; //좋아요 하트 모양 표시

    private String imageUrl;  // 이미지 URL 추가

    private String profileImageUrl; //프로필 이미지 URL
    //private boolean hasVote;  // 투표 기능 활성화 여부
    //private List<String> pollOptions; 투표 항목 리스트 추가
    //private int selectedOption; // 사용자가 선택한 옵션 (없으면 -1)
    //private String postId; , String profileImageUrl

    //feed와 group 구분 코드
    private int postId;      // 댓글 조회/등록용 ID
    private String postType;    // "feed" 또는 "group"


    //책 정보 통째로 보관
    private Book book;
    private Integer bookId;  //책 ID


    private int commentCount;     // 댓글 수
    private int likeCount;        // 좋아요 수

    private List<String> hashtags;

    private Integer rating;

    private String shareUrl; //⭐추가

    private boolean likedByMe;
    private List<String> likedUsers;



    public FeedItem(String userName, String reviewContent, String reviewDate,
                    String imageUrl, String profileImageUrl, Integer bookId,
                    int commentCount, int likeCount, String tagId) {
        this.userName = userName;
        this.reviewContent = reviewContent;
        this.reviewDate = reviewDate;
        this.imageUrl = imageUrl;
        this.profileImageUrl = profileImageUrl;
        this.bookId = bookId;
        this.commentCount = commentCount;
        this.likeCount = likeCount;
        this.tagId = tagId;
    }

    //해시태그 추가 버전(별점은 표시 안함)
    public FeedItem(String userName, String reviewContent, String reviewDate,
                    String imageUrl, String profileImageUrl, Integer bookId,
                    int commentCount, int likeCount, String tagId, List<String> hashtags) {
        this.userName = userName;
        this.reviewContent = reviewContent;
        this.reviewDate = reviewDate;
        this.imageUrl = imageUrl;
        this.profileImageUrl = profileImageUrl;
        this.bookId = bookId;
        this.commentCount = commentCount;
        this.likeCount = likeCount;
        this.tagId = tagId;
        this.hashtags = hashtags;
    }


    //기존 생성자 (댓글/좋아요 없는 버전)도 유지
    public FeedItem(String userName, String reviewContent, String reviewDate,
                    String imageUrl, String profileImageUrl, Integer bookId) {
        this(userName, reviewContent, reviewDate, imageUrl, profileImageUrl, bookId, 0, 0,null);
    }

    public FeedItem() {
        // 기본 생성자: 필드 초기화 필요 시 여기서 초기화
    }

    //별점 추가 버전
    public FeedItem(String userName, String reviewContent, String reviewDate,
                    String imageUrl, String profileImageUrl, Integer bookId,
                    int commentCount, int likeCount, String tagId, List<String> hashtags, Integer rating) {
        this.userName = userName;
        this.reviewContent = reviewContent;
        this.reviewDate = reviewDate;
        this.imageUrl = imageUrl;
        this.profileImageUrl = profileImageUrl;
        this.bookId = bookId;
        this.commentCount = commentCount;
        this.likeCount = likeCount;
        this.tagId = tagId;
        this.hashtags = hashtags;
        this.rating = rating;
    }

    // 브라우징 리뷰 생성자
    public FeedItem(String userName, String reviewContent, String reviewDate, String profileImageUrl, Integer rating) {
        this.userName = userName;
        this.reviewContent = reviewContent;
        this.reviewDate = reviewDate;
        this.profileImageUrl = profileImageUrl;
        this.rating = rating;
    }

    //⭐ 공유 URL, userId 추가 버전
    public FeedItem(String userName, String reviewContent, String reviewDate,
                    String imageUrl, String profileImageUrl, Integer bookId,
                    int commentCount, int likeCount, String tagId, List<String> hashtags, String shareUrl, int userId) {
        this.userName = userName;
        this.reviewContent = reviewContent;
        this.reviewDate = reviewDate;
        this.imageUrl = imageUrl;
        this.profileImageUrl = profileImageUrl;
        this.bookId = bookId;
        this.commentCount = commentCount;
        this.likeCount = likeCount;
        this.tagId = tagId;
        this.hashtags = hashtags;
        this.shareUrl = shareUrl;
        this.userId = userId;
    }
    //getter and setter
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
    public String getUserName() {
        return userName;
    }
    // ✅ Setter 추가
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getReviewContent() {
        return reviewContent;
    }
    //set 추가
    public void setReviewContent(String reviewContent) {
        this.reviewContent = reviewContent;
    }
    public String getReviewDate() {
        return reviewDate;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getPostType() {
        return postType;
    }

    public void setPostType(String postType) {
        this.postType = postType;
    }
    //public boolean hasVote() { return hasVote; }
    //public List<String> getPollOptions() { return pollOptions; }
    //public int getSelectedOption() { return selectedOption; }
    //public void setSelectedOption(int selectedOption) { this.selectedOption = selectedOption; }


    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    /*public boolean isLiked() { return liked; }
    public void setLiked(boolean liked) { this.liked = liked; }*/
    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }


    // ✅ 해시태그 게터/세터 추가
    public List<String> getHashtags() {
        return hashtags;
    }

    public void setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer hashtags) {
        this.rating = rating;
    }
    public String getShareUrl(){return shareUrl;} //⭐추가

    public void setShareUrl(String shareUrl) { //⭐추가
        this.shareUrl = shareUrl;
    }

    // ✅ likedByMe Getter/Setter
    public boolean isLikedByMe() { return likedByMe; }
    public void setLikedByMe(boolean likedByMe) { this.likedByMe = likedByMe; }

    // ✅ likedUsers Getter/Setter
    public List<String> getLikedUsers() { return likedUsers; }
    public void setLikedUsers(List<String> likedUsers) { this.likedUsers = likedUsers; }

}
