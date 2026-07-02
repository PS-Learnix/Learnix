package com.learnix.web.security;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AccessControlService {

    private final JdbcTemplate jdbcTemplate;

    public AccessControlService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Integer requireParentFromBearer(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new SecurityException("Token de autorizacion requerido.");
        }
        String token = authHeader.substring(7).trim();
        if (!token.startsWith("jwt-token-parent-")) {
            throw new SecurityException("Token de autorizacion invalido.");
        }
        try {
            String[] parts = token.split("-");
            if (parts.length >= 4) {
                return Integer.parseInt(parts[3]);
            }
        } catch (NumberFormatException ignored) {
            // El mensaje generico evita filtrar detalles del formato interno.
        }
        throw new SecurityException("Token de autorizacion invalido.");
    }

    public void requireSameParent(Integer expectedParentId, Integer authenticatedParentId) {
        if (expectedParentId == null || authenticatedParentId == null || !expectedParentId.equals(authenticatedParentId)) {
            throw new SecurityException("No tiene permisos para acceder a este recurso.");
        }
    }

    public void requireParentStudentAccess(Integer parentId, Integer studentId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM parent_students WHERE id_parent = ? AND id_student = ?",
                Integer.class,
                parentId,
                studentId
        );
        if (count == null || count == 0) {
            throw new SecurityException("No tiene permisos para acceder a este estudiante.");
        }
    }

    public void requireParentAnnouncementAccess(Integer parentId, Integer announcementId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM announcement_recipients WHERE id_parent = ? AND id_announcement = ?",
                Integer.class,
                parentId,
                announcementId
        );
        if (count == null || count == 0) {
            throw new SecurityException("No tiene permisos para acceder a este comunicado.");
        }
    }

    public void requireParentCitationAccess(Integer parentId, Integer citationId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM citation_recipients WHERE id_parent = ? AND id_citation = ?",
                Integer.class,
                parentId,
                citationId
        );
        if (count == null || count == 0) {
            throw new SecurityException("No tiene permisos para acceder a esta citacion.");
        }
    }
}
