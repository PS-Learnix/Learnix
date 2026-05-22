package com.learnix.web.dto;

public record UserResponse(
        Integer idUser,
        String firstName,
        String lastName,
        String email
) {}