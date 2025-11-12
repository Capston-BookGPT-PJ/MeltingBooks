package com.example.meltingbooks.network.book;

import java.io.Serializable;

public class Book implements Serializable {
    private Integer bookId; //null 허용
    private String title;
    private String cover;
    private String author;
    private String publisher;
    private String pubDate;
    private String isbn;
    private String isbn13;
    private String link;
    private String categoryName;
    private int itemPage;
    private Integer popularityScore;


    // ✅ Retrofit용 (필수: 빈 생성자)
    public Book() {}

    public Book(String title, String cover, String author, String publisher, String categoryName) {
        this.title = title;
        this.cover = cover;
        this.author = author;
        this.publisher = publisher;
        this.categoryName = categoryName;
    }

    // 2개 필드 생성자 (author 없을 때)
    public Book(String title, String cover) {
        this.title = title;
        this.cover = cover;
        this.author = ""; // 기본값
    }


    // bookId의 Getter/Setter 추가
    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    // ✅ Getter / Setter (Retrofit이 매핑할 때 필요)
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public String getCover() { return cover; }
    public void setCover(String cover) { this.cover = cover; }

    public String getPubDate() { return pubDate; }
    public void setPubDate(String pubDate) { this.pubDate = pubDate; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getIsbn13() { return isbn13; }
    public void setIsbn13(String isbn13) { this.isbn13 = isbn13; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public int getItemPage() { return itemPage; }
    public void setItemPage(int itemPage) { this.itemPage = itemPage; }


    //인기 책 ( 인용된 감상문 수)
    public Integer getPopularityScore() { return popularityScore; }
    public void setPopularityScore(Integer popularityScore) { this.popularityScore = popularityScore; }
}