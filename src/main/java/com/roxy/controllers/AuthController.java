package com.roxy.controllers;

import com.roxy.dto.AuthenticationRequest;
import com.roxy.entities.Users;
import com.roxy.services.AuthService;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;

import jakarta.inject.Inject;

@Controller("/auth")
public class AuthController {

    @Inject
    AuthService authService;

    @Post("/login")
    public HttpResponse<?> login(@Body AuthenticationRequest request) {
        Users user = authService.authenticateUser(request.getUsername(), request.getPassword());

        if (user != null) {
            return authService.generateTokensForUser(user)
                    .map(tokens -> (HttpResponse<?>) HttpResponse.ok(tokens))
                    .orElse(HttpResponse.serverError());
        } else {
            return HttpResponse.notFound().body("User not found");
        }
    }

    @Post("/logout")
    public HttpResponse<String> logout(HttpRequest<?> request) {
        return authService.handleLogout(request)
                .map(response -> HttpResponse.ok(response))
                .orElse(HttpResponse.unauthorized());
    }
}
