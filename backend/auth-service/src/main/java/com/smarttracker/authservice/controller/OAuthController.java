package com.smarttracker.authservice.controller;

import com.smarttracker.authservice.dto.AuthResponse;
import com.smarttracker.authservice.model.User;
import com.smarttracker.authservice.repository.UserRepository;
import com.smarttracker.authservice.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class OAuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @GetMapping("/oauth-success")
    public ResponseEntity<AuthResponse> oauthSuccess(OAuth2AuthenticationToken authentication) {
        // Extract email and name from Google user info
        String email = authentication.getPrincipal().getAttribute("email");
        String name = authentication.getPrincipal().getAttribute("name");

        // Create or fetch existing user
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(email)
                        .name(name)
                        .roles(List.of("USER"))
                        .isVerified(true) // Google login implies verified
                        .build()));

        // Generate access and refresh tokens
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
    }
}
