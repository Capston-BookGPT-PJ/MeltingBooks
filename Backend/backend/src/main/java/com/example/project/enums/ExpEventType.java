package com.example.project.enums;

public enum ExpEventType {
    COMPLETE_BOOK(200),          // å 1�� �ϵ�
    WRITE_REVIEW(100),           // ���� �ۼ�
    SET_MONTHLY_GOAL(200),       // ���� ��ǥ ���� (�� 1ȸ)
    SET_YEARLY_GOAL(300),        // ���� ��ǥ ���� (�� 1ȸ)
    ACHIEVE_MONTHLY_GOAL(300),   // ���� ��ǥ �޼�
    ACHIEVE_YEARLY_GOAL(1000);   // ���� ��ǥ �޼�

    private final int points;

    ExpEventType(int points) {
        this.points = points;
    }

    public int getPoints() {
        return points;
    }
}
