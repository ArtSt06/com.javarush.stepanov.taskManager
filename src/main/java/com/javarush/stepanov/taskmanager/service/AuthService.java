package com.javarush.stepanov.taskmanager.service;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.javarush.stepanov.taskmanager.dto.LoginRequest;
import com.javarush.stepanov.taskmanager.dto.RegisterRequest;
import com.javarush.stepanov.taskmanager.model.entity.User;
import com.javarush.stepanov.taskmanager.model.enums.Role;
import com.javarush.stepanov.taskmanager.model.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Пользователь с таким именем уже существует!");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public String login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Неверное имя пользователя или пароль"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Неверное имя пользователя или пароль");
        }

        return "temporary-jwt-token-for-" + user.getUsername();
    }
}
