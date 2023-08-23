package com.roxy.providers;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.AuthenticationProvider;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import com.roxy.repositories.UserRepository;

@Singleton
public class AuthenticationProviderUserPassword implements AuthenticationProvider<HttpRequest<?>> {

    private final UserRepository userRepository;

    public AuthenticationProviderUserPassword(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Publisher<AuthenticationResponse> authenticate(@Nullable HttpRequest<?> httpRequest,
            AuthenticationRequest<?, ?> authenticationRequest) {

        return Flux.create(emitter -> {
            String identity = (String) authenticationRequest.getIdentity();
            String secret = (String) authenticationRequest.getSecret();

            if (userRepository.findByUsernameAndPassword(identity, secret) != null) {
                emitter.next(AuthenticationResponse.success((String) authenticationRequest.getIdentity()));
                emitter.complete();
                System.out.println("User " + identity + " logged in successfully");
            } else {
                emitter.error(AuthenticationResponse.exception());
                System.out.println("User " + identity + " failed to log in");
            }
        }, FluxSink.OverflowStrategy.ERROR);
    }
}
