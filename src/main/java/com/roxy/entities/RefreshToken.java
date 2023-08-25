package com.roxy.entities;

import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.GeneratedValue;

import java.time.LocalDateTime;

@MappedEntity("refreshToken")
public class RefreshToken {

    @Id
    @GeneratedValue(value = GeneratedValue.Type.AUTO)
    private Long id;

    private Long userId; // This represents the user_id foreign key column in the database

    private String token;

    private LocalDateTime expiryDate;

    // getters, setters, and other methods

    public RefreshToken() {
    }

    public RefreshToken(Long userId, String token, LocalDateTime expiryDate) {
        this.userId = userId;
        this.token = token;
        this.expiryDate = expiryDate;
    }

    public RefreshToken(Long id, Long userId, String token, LocalDateTime expiryDate) {
        this.id = id;
        this.userId = userId;
        this.token = token;
        this.expiryDate = expiryDate;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setId(Long id) {
        this.id = id;
    }

}