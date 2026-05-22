package com.learnix.web.dto;

import java.time.LocalDate;

public record AttendanceRecord(
        Integer idStudent,
        LocalDate date,
        Boolean didAttend
) {}