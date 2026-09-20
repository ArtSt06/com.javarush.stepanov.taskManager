package com.javarush.stepanov.taskmanager.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.javarush.stepanov.taskmanager.dto.LoginRequest;
import com.javarush.stepanov.taskmanager.dto.RegisterRequest;
import com.javarush.stepanov.taskmanager.model.entity.User;
import com.javarush.stepanov.taskmanager.model.enums.Role;
import com.javarush.stepanov.taskmanager.model.repository.UserRepository;
import com.javarush.stepanov.taskmanager.security.JwtTokenProvider;
import com.javarush.stepanov.taskmanager.exception.InvalidCredentialsException;
import com.javarush.stepanov.taskmanager.exception.ErrorMessageConstants;
import com.javarush.stepanov.taskmanager.exception.UserAlreadyExistsException;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException(ErrorMessageConstants.USERNAME_ALREADY_TAKEN);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(ErrorMessageConstants.EMAIL_ALREADY_TAKEN);
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            throw new UserAlreadyExistsException(ErrorMessageConstants.USER_ALREADY_EXISTS);
        }
    }

    @Transactional(readOnly = true)
    public String login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException(ErrorMessageConstants.INVALID_LOGIN_OR_PASSWORD));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException(ErrorMessageConstants.INVALID_LOGIN_OR_PASSWORD);
        }

        return jwtTokenProvider.generateToken(user.getUsername(), user.getRole().name());
    }

    @Transactional(readOnly = true)
    public void verifyUserExists(String username) {
        if (!userRepository.existsByUsername(username)) {
            throw new InvalidCredentialsException(ErrorMessageConstants.INVALID_AUTH_TOKEN);
        }
    }
}
