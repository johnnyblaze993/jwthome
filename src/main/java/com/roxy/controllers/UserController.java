package com.roxy.controllers;

import com.roxy.dto.AuthenticationRequest;
import com.roxy.entities.Users;
import com.roxy.repositories.UserRepository;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;

import java.util.HashMap;
//import java.util.List;
import java.util.Optional;

@ExecuteOn(TaskExecutors.IO)
@Controller("/users")
public class UserController {

  private final UserRepository userRepository;

  public UserController(UserRepository userRepository) {
    this.userRepository = userRepository;
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
      // Return user data and a message
      return HttpResponse.ok(new HashMap<String, Object>() {
        {
          put("user", user);
          put("message", "Logged in");
        }
      });
    }

    return HttpResponse.notFound("User not found");
  }
}
