package com.learnix.web.dto;

public record DashboardStatsResponse(
        String overallCourseAverage,
        String averageCourseAttendance,
        String approvedPercentage,
        String riskPercentage
) {
    public double getSvgDashOffset() {
        try {
            double average = Double.parseDouble(overallCourseAverage);
            return 471.2 - (471.2 * (average / 4.0));
        } catch (NumberFormatException e) {
            return 471.2;
        }
    }
}