package com.example.project.enums;

public enum ExpEventType {
    COMPLETE_BOOK(200),          // 책 1권 완독
    WRITE_REVIEW(100),           // 감상문 작성
    SET_MONTHLY_GOAL(200),       // 월간 목표 설정 (월 1회)
    SET_YEARLY_GOAL(300),        // 연간 목표 설정 (연 1회)
    ACHIEVE_MONTHLY_GOAL(300),   // 월간 목표 달성
    ACHIEVE_YEARLY_GOAL(1000);   // 연간 목표 달성

    private final int points;

    ExpEventType(int points) {
        this.points = points;
    }

    public int getPoints() {
        return points;
    }
}
