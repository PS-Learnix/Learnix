package com.learnix.web.dto;

import java.time.LocalDateTime;

public record ObservationResponse(
        Integer idObservation,
        String comment,
        LocalDateTime createdAt,
        String teacherName
) {}