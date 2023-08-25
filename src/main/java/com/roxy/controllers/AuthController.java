package com.roxy.controllers;

import java.util.Optional;
import com.roxy.repositories.RefreshTokenRepository;
import com.roxy.services.AuthService;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import jakarta.inject.Inject;
import reactor.core.publisher.Mono;
import io.micronaut.security.token.jwt.validator.JwtTokenValidator;
import io.micronaut.security.token.reader.TokenReader;

@Controller("/auth")
public class AuthController {

    @Inject
    AuthService authService;

    @Inject
    RefreshTokenRepository refreshTokenRepository;

    @Inject
    TokenReader<HttpRequest<?>> tokenReader;

    @Inject
    JwtTokenValidator<HttpRequest<?>> jwtTokenValidator;

    @Post("/logout")
    public Mono<HttpResponse<String>> logout(HttpRequest<?> request) {
        Optional<String> tokenOpt = tokenReader.findToken(request);
        if (tokenOpt.isPresent()) {
            String token = tokenOpt.get();
            return Mono.from(jwtTokenValidator.validateToken(token, null))
                    .map(auth -> {
                        Long userId = (Long) auth.getAttributes().get("userId");
                        refreshTokenRepository.deleteByUserId(userId);
                        return (HttpResponse<String>) HttpResponse.ok("Logged out successfully");
                    }).onErrorReturn((HttpResponse<String>) HttpResponse.unauthorized().body("Invalid token"));
        }
        return Mono.just((HttpResponse<String>) HttpResponse.unauthorized().body("No token found"));
    }
}