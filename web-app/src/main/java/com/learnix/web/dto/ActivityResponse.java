package com.learnix.web.dto;
import java.time.LocalDateTime;

public record ActivityResponse(
        Integer idActivity,
        Integer idCoursePeriod,
        String name,
        Double weight,
        LocalDateTime createdAt
) {}