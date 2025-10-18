package com.example.meltingbooks.network.log;

public class ReadingLogRequest {
    private int pagesRead;
    private int minutesRead;
    private String readAt; // LocalDateTime ISO-8601 형식
    private boolean isFinished; // 추가

    public ReadingLogRequest(int pagesRead, int minutesRead, String readAt, boolean isFinished) {
        this.pagesRead = pagesRead;
        this.minutesRead = minutesRead;
        this.readAt = readAt;
        this.isFinished = isFinished;
    }

    public int getPagesRead() { return pagesRead; }
    public int getMinutesRead() { return minutesRead; }
    public String getReadAt() { return readAt; }
    public boolean isFinished() { return isFinished; } // getter 추가
}
