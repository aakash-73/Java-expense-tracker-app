package com.smarttracker.authservice.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.smarttracker.authservice.dto.AuthResponse;
import com.smarttracker.authservice.dto.LoginRequest;
import com.smarttracker.authservice.dto.RegisterRequest;
import com.smarttracker.authservice.model.User;
import com.smarttracker.authservice.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthResponse register(RegisterRequest request) {
        List<String> roles = request.getRoles();
        if (roles == null || roles.isEmpty()) {
            roles = List.of("USER"); // fallback
        }

        String token = UUID.randomUUID().toString();

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(encoder.encode(request.getPassword()))
                .roles(roles)
                .isVerified(false)
                .verificationToken(token)
                .build();

        userRepository.save(user);

        // 📧 Simulate sending email
        System.out.println("📩 Verification Link: http://localhost:8081/api/auth/verify-email?token=" + token);

        return new AuthResponse("Please verify your email before logging in.", null);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isVerified()) {
            throw new RuntimeException("❌ Please verify your email before logging in.");
        }

        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return new AuthResponse(accessToken, refreshToken);
    }
}
