package com.roxy.controllers;

import com.roxy.dto.AuthenticationRequest;
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

import reactor.core.publisher.Mono;

import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Optional;

@ExecuteOn(TaskExecutors.IO)
@Controller("/users")
public class UserController {

  private final UserRepository userRepository;
  private final TokenGenerator tokenGenerator;
  private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

  @Inject
  private TokenValidator<?> tokenValidator;

  public UserController(UserRepository userRepository, TokenGenerator tokenGenerator) {
    this.userRepository = userRepository;
    this.tokenGenerator = tokenGenerator;
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
      // Generate a token for the user
      Optional<String> userToken = tokenGenerator.generateToken(new HashMap<String, Object>() {
        {
          put("sub", user.username()); // Include the 'sub' claim using the correct accessor
          put("userId", user.id()); // Embed the user's ID in the token
          // You can add more claims as needed
        }
      });

      if (userToken.isPresent()) {
        return HttpResponse.ok(new HashMap<String, Object>() {
          {
            put("user", user);
            put("token", userToken.get());
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
