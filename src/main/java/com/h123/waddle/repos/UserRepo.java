package com.h123.waddle.repos;

import com.h123.waddle.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User,Long>
{
    Optional<User> findByEmail(String email);
    Optional<User> findByName(String name);
    boolean existsByName(String name);
    boolean existsByEmail(String email);
}
