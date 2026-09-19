package com.javarush.stepanov.taskmanager.model.repository;

import java.util.Optional;

import com.javarush.stepanov.taskmanager.model.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
