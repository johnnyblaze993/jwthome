package com.roxy.controllers;

import java.util.Optional;

import com.roxy.entities.Users;
import com.roxy.repositories.UserRepository;

import io.micronaut.http.annotation.*;

import jakarta.inject.Inject;

@Controller("/users")
public class UserController {

  @Inject
  private UserRepository userRepository;

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
    Users updatedUser = new Users(id, users.username(), users.password());
    return userRepository.update(updatedUser);
  }

  @Delete("/{id}")
  public void delete(Long id) {
    userRepository.deleteById(id);
  }
}
