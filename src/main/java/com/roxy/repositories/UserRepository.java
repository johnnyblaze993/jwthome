package com.roxy.repositories;

import com.roxy.entities.Users;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
// import java.util.UUID;

@JdbcRepository(dialect = Dialect.POSTGRES) // specify the dialect here
public interface UserRepository extends CrudRepository<Users, Long> {
    // your repository methods
    @Query("SELECT * FROM jwt_auth.users WHERE username = :username AND password = :password")
    Users findByUsernameAndPassword(String username, String password);

}