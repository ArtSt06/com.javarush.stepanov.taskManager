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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Ошибка регистрации: имя пользователя \"{}\" уже занято", request.getUsername());
            throw new UserAlreadyExistsException(ErrorMessageConstants.USERNAME_ALREADY_TAKEN);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Ошибка регистрации: Email \"{}\" уже занят", request.getEmail());
            throw new UserAlreadyExistsException(ErrorMessageConstants.EMAIL_ALREADY_TAKEN);
        }

        log.info("Регистрация нового пользователя: \"{}\" : {}", request.getUsername(), request.getEmail());

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            log.error("Ошибка при сохранении пользователя: \"{}\" - такой пользователь уже существует", request.getUsername());
            throw new UserAlreadyExistsException(ErrorMessageConstants.USER_ALREADY_EXISTS);
        }
    }

    @Transactional(readOnly = true)
    public String login(LoginRequest request) {
        log.info("Запрос на вход в систему от пользователя: \"{}\"", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.warn("Неудачная попытка входа: пользователь \"{}\" не найден в базе данных", request.getUsername());
                    return new InvalidCredentialsException(ErrorMessageConstants.INVALID_LOGIN_OR_PASSWORD);
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Неудачная попытка входа: неверный пароль для пользователя \"{}\"", request.getUsername());
            throw new InvalidCredentialsException(ErrorMessageConstants.INVALID_LOGIN_OR_PASSWORD);
        }

        log.info("Пользователь \"{}\" успешно аутентифицирован", request.getUsername());
        return jwtTokenProvider.generateToken(user.getUsername(), user.getRole().name());
    }

    @Transactional(readOnly = true)
    public void verifyUserExists(String username) {
        if (!userRepository.existsByUsername(username)) {
            log.error("Пользователем \"{}\" произведен неудачный запрос по токену", username);
            throw new InvalidCredentialsException(ErrorMessageConstants.INVALID_AUTH_TOKEN);
        }
    }
}
