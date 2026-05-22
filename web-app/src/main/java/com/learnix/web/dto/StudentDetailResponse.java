package com.learnix.web.dto;
import java.time.LocalDate;

public record StudentDetailResponse(
        Integer idStudent,
        String firstName,
        String lastName,
        String dni,
        LocalDate birthDate
) {}