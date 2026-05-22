package com.learnix.web.dto;

public record TeacherProfileCourse(
        Integer idCourse,
        String name,
        int totalSections
) {}