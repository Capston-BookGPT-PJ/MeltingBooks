package com.example.meltingbooks.network.log;

public class ReadingLogResponse {
    private int id;
    private int userId;
    private int bookId;
    private int pagesRead;
    private int minutesRead;
    private String readAt; // "2025-09-23T00:00:00"
    private boolean isFinished; // 추가

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getBookId() { return bookId; }
    public int getPagesRead() { return pagesRead; }
    public int getMinutesRead() { return minutesRead; }
    public String getReadAt() { return readAt; }
    public boolean isFinished() { return isFinished; } // getter 추가
}
