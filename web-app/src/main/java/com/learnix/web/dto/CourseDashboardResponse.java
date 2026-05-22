package com.learnix.web.dto;

public record CourseDashboardResponse(
        Integer idCourse,
        String name,
        Integer idCoursePeriod,
        String section
) {}