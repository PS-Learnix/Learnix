package com.learnix.web.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final MobileAuthInterceptor mobileAuthInterceptor;

    @Value("${learnix.security.allowed-origins:http://localhost:3000,http://localhost:5173,http://localhost:8080}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/mobile/**")
                .allowedOriginPatterns(parseAllowedOrigins())
                .allowedMethods("GET", "POST", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Content-Disposition")
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(mobileAuthInterceptor)
                .addPathPatterns("/api/mobile/**")
                .excludePathPatterns("/api/mobile/auth/login");

        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public void postHandle(HttpServletRequest request, HttpServletResponse response,
                    Object handler, ModelAndView modelAndView) throws Exception {
                response.setHeader("X-Content-Type-Options", "nosniff");
                response.setHeader("X-Frame-Options", "DENY");
                response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
                response.setHeader("Permissions-Policy", "geolocation=(), microphone=(), camera=()");
                if (modelAndView != null) {
                    modelAndView.addObject("currentUri", request.getRequestURI());
                }
            }
        });
    }

    private String[] parseAllowedOrigins() {
        return allowedOrigins.split("\\s*,\\s*");
    }
}
