package com.learnix.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
public class HomeController {

    @Value("${spring.application.name}")
    private String appName;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getIndex() {

        Map<String, Object> response = new HashMap<>();
        response.put("name", appName);
        response.put("version", "0.0.1");
        response.put("timestamp", LocalDateTime.now());
        response.put("endpoint_docs", "/swagger-ui/index.html");

        return ResponseEntity.ok().body(response);
    }
}