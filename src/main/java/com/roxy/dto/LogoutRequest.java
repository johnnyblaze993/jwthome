package com.roxy.dto;

public class LogoutRequest {

    private Long userId;
    private String token;

    // Constructors, getters, setters, etc.

    public LogoutRequest() {
    }

    public LogoutRequest(Long userId, String token) {
        this.userId = userId;
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "LogoutRequest [userId=" + userId + ", token=" + token + "]";
    }
}