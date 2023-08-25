package com.roxy.controllers;

import com.roxy.entities.RefreshToken;
import com.roxy.repositories.RefreshTokenRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;

import jakarta.inject.Inject;
import java.util.Optional;

@Controller("/refreshtokens")
public class RefreshTokenController {

    @Inject
    private RefreshTokenRepository refreshTokenRepository;

    @Get("/{id}")
    public Optional<RefreshToken> show(Long id) {
        return refreshTokenRepository.findById(id);
    }

    @Delete("/user/{userId}")
    public HttpResponse<?> deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
        return HttpResponse.status(HttpStatus.NO_CONTENT);
    }

    @Delete("/token/{token}")
    public HttpResponse<?> deleteByToken(String token) {
        Optional<RefreshToken> optionalToken = refreshTokenRepository.findByToken(token);
        optionalToken.ifPresent(refreshTokenRepository::delete);
        return HttpResponse.status(HttpStatus.NO_CONTENT);
    }

    // ... other methods
}