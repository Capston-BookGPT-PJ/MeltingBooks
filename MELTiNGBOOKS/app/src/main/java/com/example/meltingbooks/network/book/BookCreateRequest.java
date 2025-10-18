package com.example.meltingbooks.network.book;

public class BookCreateRequest {
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

    public BookCreateRequest(String title, String author, String publisher, String pubDate,
                             String isbn, String isbn13, String cover, String link,
                             String categoryName, int itemPage) {
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.pubDate = pubDate;
        this.isbn = isbn;
        this.isbn13 = isbn13;
        this.cover = cover;
        this.link = link;
        this.categoryName = categoryName;
        this.itemPage = itemPage;
    }

    // ---------------------
    // Getter / Setter
    // ---------------------
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public String getPubDate() { return pubDate; }
    public void setPubDate(String pubDate) { this.pubDate = pubDate; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getIsbn13() { return isbn13; }
    public void setIsbn13(String isbn13) { this.isbn13 = isbn13; }

    public String getCover() { return cover; }
    public void setCover(String cover) { this.cover = cover; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public int getItemPage() { return itemPage; }
    public void setItemPage(int itemPage) { this.itemPage = itemPage; }
}
