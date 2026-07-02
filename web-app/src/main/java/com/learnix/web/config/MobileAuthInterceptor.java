package com.learnix.web.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class MobileAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Permit CORS preflight requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Token de autorizacion requerido o invalido\"}");
            return false;
        }

        String token = authHeader.substring(7).trim();
        // A simple token validation: must start with "jwt-token-parent-"
        if (!token.startsWith("jwt-token-parent-")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Formato del token invalido\"}");
            return false;
        }

        Integer parentId = extractParentId(token);
        if (parentId == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Formato del token invalido\"}");
            return false;
        }

        request.setAttribute("authenticatedParentId", parentId);
        return true;
    }

    private Integer extractParentId(String token) {
        try {
            String[] parts = token.split("-");
            if (parts.length >= 4) {
                return Integer.parseInt(parts[3]);
            }
        } catch (NumberFormatException ignored) {
            // La validacion fina se ejecuta en los controladores/servicios que usan el ID.
        }
        return null;
    }
}
