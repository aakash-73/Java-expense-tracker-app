package com.smarttracker.authservice.controller;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smarttracker.authservice.dto.AuthResponse;
import com.smarttracker.authservice.dto.LoginRequest;
import com.smarttracker.authservice.dto.RegisterRequest;
import com.smarttracker.authservice.model.BlacklistedToken;
import com.smarttracker.authservice.model.User;
import com.smarttracker.authservice.repository.BlacklistedTokenRepository;
import com.smarttracker.authservice.repository.UserRepository;
import com.smarttracker.authservice.service.AuthService;
import com.smarttracker.authservice.service.JwtService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        String email = jwtService.extractUsername(refreshToken);

        if (email == null || !jwtService.isTokenValid(refreshToken, email)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository.findByEmail(email).orElseThrow();
        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        return ResponseEntity.ok(new AuthResponse(newAccessToken, newRefreshToken));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        Optional<User> optionalUser = userRepository.findByVerificationToken(token);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid verification token.");
        }

        User user = optionalUser.get();
        user.setVerified(true);
        user.setVerificationToken(null); // clear token
        userRepository.save(user);

        return ResponseEntity.ok("✅ Email verified successfully! You can now log in.");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body("❌ User not found.");
        }

        User user = optionalUser.get();
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(System.currentTimeMillis() + 1000 * 60 * 15); // 15 mins
        userRepository.save(user);

        System.out.println("📩 Reset Link: http://localhost:8081/api/auth/reset-password?token=" + token);
        return ResponseEntity.ok("✅ Password reset link sent (check console).");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestBody Map<String, String> body) {
        Optional<User> optionalUser = userRepository.findByResetToken(token);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body("❌ Invalid reset token.");
        }

        User user = optionalUser.get();

        if (user.getResetTokenExpiry() != null && user.getResetTokenExpiry() < System.currentTimeMillis()) {
            return ResponseEntity.badRequest().body("❌ Reset token expired.");
        }

        String newPassword = body.get("newPassword");
        user.setPassword(new BCryptPasswordEncoder().encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        return ResponseEntity.ok("✅ Password reset successful!");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody Map<String, String> body) {
        String accessToken = body.get("accessToken");
        String refreshToken = body.get("refreshToken");

        if (accessToken == null || refreshToken == null) {
            return ResponseEntity.badRequest().body("❌ Access and refresh tokens are required.");
        }

        Date accessExpiry = jwtService.extractExpiration(accessToken);
        Date refreshExpiry = jwtService.extractExpiration(refreshToken);

        blacklistedTokenRepository.save(new BlacklistedToken(null, accessToken, accessExpiry));
        blacklistedTokenRepository.save(new BlacklistedToken(null, refreshToken, refreshExpiry));

        return ResponseEntity.ok("✅ Logged out. Access & Refresh tokens blacklisted.");
    }

}
