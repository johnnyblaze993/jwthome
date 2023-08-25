package com.roxy.controllers;

import com.roxy.dto.AuthenticationRequest;
import com.roxy.entities.RefreshToken;
import com.roxy.entities.Users;
import com.roxy.repositories.UserRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.token.generator.TokenGenerator;
import io.micronaut.security.token.validator.TokenValidator;
import io.micronaut.security.authentication.Authentication;
import org.reactivestreams.Publisher;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.roxy.repositories.RefreshTokenRepository;
import java.security.SecureRandom;
import java.util.Base64;

import reactor.core.publisher.Mono;

import jakarta.inject.Inject;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;

@ExecuteOn(TaskExecutors.IO)
@Controller("/users")
public class UserController {

  private final UserRepository userRepository;
  private final TokenGenerator tokenGenerator;
  private final RefreshTokenRepository refreshTokenRepository;
  private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

  @Inject
  private TokenValidator<?> tokenValidator;

  public UserController(UserRepository userRepository, TokenGenerator tokenGenerator,
      RefreshTokenRepository refreshTokenRepository) {
    this.userRepository = userRepository;
    this.tokenGenerator = tokenGenerator;
    this.refreshTokenRepository = refreshTokenRepository;
  }

  @Get("/")
  public Iterable<Users> index() {
    return userRepository.findAll();
  }

  @Get("/{id}")
  public Optional<Users> show(Long id) {
    return userRepository.findById(id);
  }

  @Post("/")
  public Users create(@Body Users users) {
    return userRepository.save(users);
  }

  @Put("/{id}")
  public Users update(Long id, @Body Users users) {
    // Since records are immutable, create a new instance with the desired values
    Users updatedUser = new Users(id, users.username(), users.password());
    return userRepository.update(updatedUser);
  }

  @Delete("/{id}")
  public void delete(Long id) {
    userRepository.deleteById(id);
  }

  @Post("/login")
  public HttpResponse<?> login(@Body AuthenticationRequest request) {
    Users user = userRepository.findByUsernameAndPassword(request.getUsername(), request.getPassword());

    if (user != null) {
      // Generate the JWT token for the user
      long jwtExpirationTimeMillis = System.currentTimeMillis() + (60 * 1000); // 1 minute from now
      Date jwtExpiration = new Date(jwtExpirationTimeMillis);

      Optional<String> userToken = tokenGenerator.generateToken(new HashMap<String, Object>() {
        {
          put("sub", user.username());
          put("userId", user.id());
          put("exp", jwtExpiration); // Explicitly setting expiration
        }
      });

      if (userToken.isPresent()) {
        // Generate the refresh token for the user
        long jwtRefreshExpirationTimeMillis = System.currentTimeMillis() + (60 * 2000); // 2 minutes from now
        Date refreshExpiration = new Date(jwtRefreshExpirationTimeMillis);
        Optional<String> refreshToken = tokenGenerator.generateToken(new HashMap<String, Object>() {
          {
            put("sub", user.username());
            put("userId", user.id());
            put("exp", refreshExpiration); // Explicitly setting expiration
          }
        });

        // Fetch the refresh token from the database
        RefreshToken existingToken = refreshTokenRepository.findByUserId(user.id()).orElse(null);
        LocalDateTime refreshExpirationDateTime = refreshExpiration.toLocalDate().atStartOfDay();

        if (existingToken != null) {
          // Update existing token
          existingToken.setToken(refreshToken.get());
          existingToken.setExpiryDate(refreshExpirationDateTime);
          refreshTokenRepository.update(existingToken); // assuming you have an update method
        } else {
          // Create a new token
          RefreshToken newRefreshToken = new RefreshToken(user.id(), refreshToken.get(), refreshExpirationDateTime);
          refreshTokenRepository.save(newRefreshToken);
        }

        // Return the JWT token and refresh token to the user
        return HttpResponse.ok(new HashMap<String, Object>() {
          {
            put("user", user);
            put("token", userToken.get());
            put("refreshToken", refreshToken.get());
            put("message", "Logged in");
          }
        });
      } else {
        return HttpResponse.serverError("Failed to generate token");
      }
    }
    return HttpResponse.notFound("User not found");
  }

  @Post("/validateToken")
  public HttpResponse<?> validateToken(@Header String Authorization) {
    LOG.info("Entered validateToken method");
    LOG.info("Validating token with Authorization header: {}", Authorization);

    String token = Authorization.replace("Bearer ", "");
    LOG.info("Extracted token: {}", token);

    Publisher<Authentication> result = tokenValidator.validateToken(token, null);
    return Mono.from(result)
        .doOnSuccess(auth -> LOG.info("Token validation succeeded"))
        .doOnError(err -> LOG.error("Token validation failed", err))
        .map(auth -> HttpResponse.ok("Token is valid"))
        .onErrorReturn(HttpResponse.unauthorized())
        .block();
  }

}
