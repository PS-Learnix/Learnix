package com.learnix.web.dto;

public record ReportCardItem(
        String activityName,
        Double activityWeight,
        String gradeValue
) {}