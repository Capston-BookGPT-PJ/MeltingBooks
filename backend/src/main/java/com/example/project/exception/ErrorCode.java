package com.example.project.exception;

public enum ErrorCode {
    USER_NOT_FOUND("USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
    BOOK_NOT_FOUND("BOOK_NOT_FOUND", "책을 찾을 수 없습니다."),
    LOG_NOT_FOUND("LOG_NOT_FOUND", "독서 기록을 찾을 수 없습니다."),
    INTERNAL_ERROR("INTERNAL_ERROR", "서버 내부 오류가 발생했습니다.");

    private final String code;
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() { return code; }
    public String getDefaultMessage() { return defaultMessage; }
}
