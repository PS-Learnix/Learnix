package com.learnix.web.dto;

public record TeacherStatsResponse(
        int totalCourses,
        int totalStudents
) {}