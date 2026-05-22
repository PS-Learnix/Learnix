package com.learnix.web.dto;

public record StudentResponse(
        Integer idStudent,
        String firstName,
        String lastName,
        String dni
) {}
