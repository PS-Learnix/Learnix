package com.learnix.web.controller.rest;

import com.learnix.web.dto.MobileDtos.LoginRequest;
import com.learnix.web.dto.MobileDtos.LoginResponse;
import com.learnix.web.service.MobileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mobile/auth")
@RequiredArgsConstructor
public class MobileAuthController {

    private final MobileService mobileService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        System.out.println("--> MobileAuthController: login request for email=" + request.email());
        if (request.email() == null || request.password() == null) {
            System.out.println("<-- MobileAuthController: login bad request (missing fields)");
            return ResponseEntity.badRequest().body(Map.of("error", "Email and password are required"));
        }
        
        LoginResponse response = mobileService.login(request.email(), request.password());
        if (response == null) {
            System.out.println("<-- MobileAuthController: login unauthorized (invalid credentials)");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials", "message", "Email o contraseña incorrectos"));
        }
        
        System.out.println("<-- MobileAuthController: login successful for parentId=" + (response.parent() != null ? response.parent().id() : null) + ", token=" + response.token());
        return ResponseEntity.ok(response);
    }
}
