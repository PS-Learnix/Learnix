package com.learnix.web.dto;

public record ActivityDetailResponse(
        Integer idActivity,
        String activityName,
        String courseName
) {}