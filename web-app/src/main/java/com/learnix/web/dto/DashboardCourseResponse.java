package com.learnix.web.dto;

public record DashboardCourseResponse(
        Integer idCourse,
        String name,
        Integer idCoursePeriod,
        String section
) {}