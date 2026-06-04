package com.learnix.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserResponse(
        @JsonProperty("o_id_user")
        Integer idUser,
        @JsonProperty("o_first_name")
        String firstName,
        @JsonProperty("o_last_name")
        String lastName,
        @JsonProperty("o_email")
        String email
) {}