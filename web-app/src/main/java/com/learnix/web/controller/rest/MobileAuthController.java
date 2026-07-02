package com.learnix.web.controller.rest;

import com.learnix.web.dto.MobileDtos.LoginRequest;
import com.learnix.web.dto.MobileDtos.LoginResponse;
import com.learnix.web.security.SecurityInputValidator;
import com.learnix.web.service.MobileService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/mobile/auth")
@RequiredArgsConstructor
public class MobileAuthController {

    private static final Logger log = LoggerFactory.getLogger(MobileAuthController.class);

    private final MobileService mobileService;
    private final SecurityInputValidator inputValidator;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (request == null || request.email() == null || request.password() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email and password are required"));
        }

        String email = inputValidator.requireSafeText(request.email(), "email", 150);
        String password = inputValidator.requireSafeText(request.password(), "password", 255);
        log.info("security_event type=mobile_login_attempt email={}", maskEmail(email));

        LoginResponse response = mobileService.login(email, password);
        if (response == null) {
            log.warn("security_event type=mobile_login_failed email={}", maskEmail(email));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials", "message", "Email o password incorrectos"));
        }

        log.info("security_event type=mobile_login_success parentId={}",
                response.parent() != null ? response.parent().id() : null);
        return ResponseEntity.ok(response);
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(at);
    }
}
