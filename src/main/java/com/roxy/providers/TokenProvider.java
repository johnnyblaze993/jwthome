package com.roxy.providers;

import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.token.generator.TokenGenerator;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class TokenProvider {

    @Inject
    private TokenGenerator tokenGenerator;

    public Publisher<String> generateToken(Authentication authentication) {
        if (authentication != null && authentication.getName() != null) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("username", authentication.getName());
            // Add any additional claims as needed

            return Mono.justOrEmpty(tokenGenerator.generateToken(claims));
        } else {
            return Mono.empty();
        }
    }
}