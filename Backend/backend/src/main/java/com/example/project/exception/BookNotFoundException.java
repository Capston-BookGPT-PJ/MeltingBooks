package com.example.project.exception;

public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(Long bookId) {
        super("책을 찾을 수 없습니다. (ID: " + bookId + ")");
    }
}
