package com.learnix.web.dto;

import java.util.List;

public record StudentAttendanceRow(
        Integer idStudent,
        String firstName,
        String lastName,
        List<String> states
) {}