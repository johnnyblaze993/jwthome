package com.roxy.services;

import java.time.LocalDateTime;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import com.roxy.entities.RefreshToken;
import com.roxy.repositories.RefreshTokenRepository;

@Singleton
public class AuthService {

    @Inject
    RefreshTokenRepository refreshTokenRepository;

    public void generateRefreshTokenForUser(Long userId, String newToken, LocalDateTime expiryDate) {
        RefreshToken existingToken = findRefreshTokenByUserId(userId);
        if (existingToken != null) {
            // Update existing token
            existingToken.setToken(newToken);
            existingToken.setExpiryDate(expiryDate);
            refreshTokenRepository.save(existingToken);
        } else {
            // Create a new token
            RefreshToken refreshToken = new RefreshToken(userId, newToken, expiryDate);
            refreshTokenRepository.save(refreshToken);
        }
    }

    public RefreshToken findRefreshTokenByUserId(Long userId) {
        return refreshTokenRepository.findByUserId(userId).orElse(null);
    }
}