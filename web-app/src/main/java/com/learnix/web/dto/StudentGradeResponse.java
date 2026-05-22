package com.learnix.web.dto;

public record StudentGradeResponse(
        Integer idStudent,
        String firstName,
        String lastName,
        String gradeValue
) {
    public boolean isApproved() {
        return gradeValue != null && (gradeValue.equals("AD") || gradeValue.equals("A"));
    }
}