package com.roxy.entities;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import jakarta.validation.constraints.NotNull;

// import java.util.UUID;

@MappedEntity
public record Users(
        @Id @NotNull Long id,
        @NotNull String username,
        @NotNull String password) {

    public Object getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

}