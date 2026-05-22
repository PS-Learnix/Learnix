package com.learnix.web.dto;

import java.time.LocalDate;

public record DayOfWeekDto(
        String name,
        LocalDate date,
        boolean isToday) {
}
