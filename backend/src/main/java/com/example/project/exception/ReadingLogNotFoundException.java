package com.example.project.exception;

public class ReadingLogNotFoundException extends RuntimeException {
    public ReadingLogNotFoundException(Long logId) {
        super("독서 기록을 찾을 수 없습니다. (ID: " + logId + ")");
    }
}
