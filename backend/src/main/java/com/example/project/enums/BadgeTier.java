package com.example.project.enums;

public enum BadgeTier {
    BRONZE, SILVER, GOLD, PLATINUM;

    public boolean higherThan(BadgeTier other) {
        return this.ordinal() > other.ordinal();
    }
}