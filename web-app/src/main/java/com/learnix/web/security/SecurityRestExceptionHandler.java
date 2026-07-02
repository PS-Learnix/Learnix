package com.learnix.web.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.format.DateTimeParseException;
import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.learnix.web.controller.rest")
public class SecurityRestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(SecurityRestExceptionHandler.class);

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> handleSecurity(SecurityException ex) {
        log.warn("security_event type=access_denied message={}", ex.getMessage());
        return build(HttpStatus.FORBIDDEN, "ACCESS_DENIED", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(IllegalArgumentException ex) {
        log.warn("security_event type=validation_error message={}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage());
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            DateTimeParseException.class
    })
    public ResponseEntity<Map<String, Object>> handleBadRequest(Exception ex) {
        log.warn("security_event type=bad_request class={}", ex.getClass().getSimpleName());
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Solicitud invalida o incompleta.");
    }

    @ExceptionHandler(UncategorizedSQLException.class)
    public ResponseEntity<Map<String, Object>> handleSql(UncategorizedSQLException ex) {
        log.warn("security_event type=database_rejected message={}", safeSqlMessage(ex));
        return build(HttpStatus.CONFLICT, "CONFLICT", "La operacion no pudo procesarse.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("security_event type=unexpected_error class={}", ex.getClass().getSimpleName());
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Ocurrio un error controlado.");
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "code", code,
                "message", message,
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    private String safeSqlMessage(UncategorizedSQLException ex) {
        return ex.getSQLException() != null ? ex.getSQLException().getSQLState() : ex.getClass().getSimpleName();
    }
}
