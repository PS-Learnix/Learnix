package com.learnix.web.security;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.regex.Pattern;

@Component
public class SecurityInputValidator {

    private static final Pattern SUSPICIOUS_INPUT = Pattern.compile(
            "(?i)(<\\s*script|</\\s*script|javascript:|\\bunion\\b\\s+\\bselect\\b|\\bdrop\\b\\s+\\btable\\b|--|/\\*|\\*/)"
    );

    public Integer requirePositive(Integer value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " debe ser un entero positivo.");
        }
        return value;
    }

    public String optionalSafeText(String value, String fieldName, int maxLength) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " supera la longitud permitida.");
        }
        if (SUSPICIOUS_INPUT.matcher(trimmed).find()) {
            throw new IllegalArgumentException(fieldName + " contiene caracteres no permitidos.");
        }
        return trimmed;
    }

    public String requireSafeText(String value, String fieldName, int maxLength) {
        String trimmed = optionalSafeText(value, fieldName, maxLength);
        if (trimmed == null || trimmed.isBlank()) {
            throw new IllegalArgumentException(fieldName + " es obligatorio.");
        }
        return trimmed;
    }

    public String optionalAllowedValue(String value, String fieldName, Set<String> allowedValues) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String normalized = value.trim().toLowerCase();
        if (!allowedValues.contains(normalized)) {
            throw new IllegalArgumentException(fieldName + " no tiene un valor permitido.");
        }
        return normalized;
    }
}
