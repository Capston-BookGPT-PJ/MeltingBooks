package com.example.project.util;

public final class GoalFormatUtils {
    private GoalFormatUtils() {}

    public static String books(int completed, int target) {
        return completed + "권/" + target + "권";
    }

    public static String reviews(int completed, int target) {
        return completed + "개/" + target + "개";
    }

    public static String minutes(int completed, int target) {
        return toHourMin(completed) + "/" + toHourMin(target);
    }

    private static String toHourMin(int minutes) {
        int h = minutes / 60;
        int m = minutes % 60;
        if (h > 0 && m > 0) return h + "시간 " + m + "분";
        if (h > 0) return h + "시간";
        return m + "분";
    }
}
