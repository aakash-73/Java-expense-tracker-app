package com.smarttracker.authservice.service;

import java.util.Date;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.smarttracker.authservice.repository.BlacklistedTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenCleanupService {

    private final BlacklistedTokenRepository blacklistedTokenRepository;

    // Run every hour
    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void cleanExpiredTokens() {
        Date now = new Date();
        blacklistedTokenRepository.deleteByExpiryDateBefore(now);
        System.out.println("🧹 Cleaned up expired blacklisted tokens at: " + now);
    }
}
