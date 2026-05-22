package com.learnix.web.dto;

public record ActivityStatsResponse(
        int totalStudents,
        int approvedCount,
        int disapprovedCount
) {
    public double getApprovedPercentage() {
        return totalStudents == 0 ? 0 : ((double) approvedCount / totalStudents) * 100;
    }
    public double getDisapprovedPercentage() {
        return totalStudents == 0 ? 0 : ((double) disapprovedCount / totalStudents) * 100;
    }
}