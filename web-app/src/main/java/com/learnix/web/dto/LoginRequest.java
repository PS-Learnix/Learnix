package com.learnix.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
                @NotBlank(message = "El correo es obligatorio") @Email(message = "Email no válido") String email,

                @NotBlank(message = "La contraseña es obligatoria") String password,

                boolean rememberMe) {
}