package com.example.project.exception;

public class UserNotFoundException extends RuntimeException {
    // Long 타입용
    public UserNotFoundException(Long userId) {
        super("사용자를 찾을 수 없습니다. ID=" + userId);
    }

    // String 메시지 직접 전달용
    public UserNotFoundException(String message) {
        super(message);
    }
}
