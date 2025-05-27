package com.smarttracker.authservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/dashboard")
    public ResponseEntity<String> dashboard(Authentication authentication) {
        System.out.println("🔒 Admin Endpoint Hit");
        System.out.println("🔐 Authenticated user: " + authentication.getName());
        System.out.println("🔐 Granted authorities: " + authentication.getAuthorities());
        return ResponseEntity.ok("✅ Welcome to the Admin Dashboard!");
    }

}
