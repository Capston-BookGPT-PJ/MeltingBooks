package com.example.project.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public final class DateRuleUtils {
    private DateRuleUtils(){}

    public static boolean isConsecutiveMonthsSatisfied(LocalDate now, int months,
                                                       Predicate<YearMonth> perMonthCheck) {
        YearMonth current = YearMonth.from(now);
        return IntStream.range(0, months)
                .mapToObj(current::minusMonths)
                .allMatch(perMonthCheck);
    }
}
