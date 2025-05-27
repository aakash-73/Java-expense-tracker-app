package com.smarttracker.authservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/protected")
public class TestController {

    @GetMapping
    public ResponseEntity<String> getProtectedResource() {
        return ResponseEntity.ok("✅ Access granted to protected resource!");
    }
}
