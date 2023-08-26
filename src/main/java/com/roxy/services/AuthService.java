package com.roxy.services;

import com.roxy.entities.RefreshToken;
import com.roxy.entities.Users;
import com.roxy.repositories.UserRepository;
import com.roxy.repositories.RefreshTokenRepository;

import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.token.generator.TokenGenerator;
import io.micronaut.security.token.jwt.validator.JwtTokenValidator;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;
import io.micronaut.http.HttpRequest;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.micronaut.security.token.reader.TokenReader;

//
@Singleton
public class AuthService {

    @Inject
    UserRepository userRepository;

    @Inject
    RefreshTokenRepository refreshTokenRepository;

    @Inject
    TokenGenerator tokenGenerator;

    @Inject
    TokenReader<HttpRequest<?>> tokenReader;

    @Inject
    JwtTokenValidator<HttpRequest<?>> jwtTokenValidator;

    public Users authenticateUser(String username, String password) {
        return userRepository.findByUsernameAndPassword(username, password);
    }

    public Optional<Map<String, Object>> generateTokensForUser(Users user) {
        // JWT token generation
        long jwtExpirationTimeMillis = System.currentTimeMillis() + (60 * 1000); // 1 minute from now
        Date jwtExpiration = new Date(jwtExpirationTimeMillis);

        Optional<String> userToken = tokenGenerator.generateToken(new HashMap<String, Object>() {
            {
                put("sub", user.username());
                put("userId", user.id());
                put("exp", jwtExpiration);
            }
        });

        if (userToken.isPresent()) {
            // Refresh token generation
            long jwtRefreshExpirationTimeMillis = System.currentTimeMillis() + (60 * 2000); // 2 minutes from now
            Date refreshExpiration = new Date(jwtRefreshExpirationTimeMillis);
            Optional<String> refreshToken = tokenGenerator.generateToken(new HashMap<String, Object>() {
                {
                    put("sub", user.username());
                    put("userId", user.id());
                    put("exp", refreshExpiration);
                }
            });

            // Storing the refresh token in the database
            LocalDateTime refreshExpirationDateTime = refreshExpiration.toLocalDate().atStartOfDay();
            RefreshToken existingToken = refreshTokenRepository.findByUserId(user.id()).orElse(null);

            if (existingToken != null) {
                existingToken.setToken(refreshToken.get());
                existingToken.setExpiryDate(refreshExpirationDateTime);
                refreshTokenRepository.update(existingToken);
            } else {
                RefreshToken newRefreshToken = new RefreshToken(user.id(), refreshToken.get(),
                        refreshExpirationDateTime);
                refreshTokenRepository.save(newRefreshToken);
            }

            // Returning the tokens
            Map<String, Object> tokens = new HashMap<>();
            tokens.put("token", userToken.get());
            tokens.put("refreshToken", refreshToken.get());
            tokens.put("user", user);
            tokens.put("message", "Logged in successfully");
            return Optional.of(tokens);
        }

        return Optional.empty();
    }

    public Optional<String> handleLogout(HttpRequest<?> request) {
        Optional<String> tokenOpt = tokenReader.findToken(request);

        if (tokenOpt.isPresent()) {
            String token = tokenOpt.get();

            Authentication authentication = Mono.from(jwtTokenValidator.validateToken(token, null))
                    .block();

            if (authentication == null) {
                return Optional.empty();
            }

            Long userId = (Long) authentication.getAttributes().get("userId");
            refreshTokenRepository.deleteByUserId(userId);
            return Optional.of("Logged out successfully");
        }

        return Optional.empty();
    }
}
