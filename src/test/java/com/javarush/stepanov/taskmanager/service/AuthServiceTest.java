package com.javarush.stepanov.taskmanager.service;

import java.util.Optional;

import com.javarush.stepanov.taskmanager.dto.LoginRequest;
import com.javarush.stepanov.taskmanager.dto.RegisterRequest;
import com.javarush.stepanov.taskmanager.exception.InvalidCredentialsException;
import com.javarush.stepanov.taskmanager.exception.UserAlreadyExistsException;
import com.javarush.stepanov.taskmanager.model.entity.User;
import com.javarush.stepanov.taskmanager.model.enums.Role;
import com.javarush.stepanov.taskmanager.model.repository.UserRepository;
import com.javarush.stepanov.taskmanager.security.JwtTokenProvider;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("test-user");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("test-user");
        loginRequest.setPassword("password");

        testUser = User.builder()
                .id(1L)
                .username("test-user")
                .email("test@example.com")
                .password("encoded_password_hash")
                .role(Role.USER)
                .build();
    }

    @Test
    void should_RegisterUser_When_UsernameAndEmailAreAvailable() {
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encoded_password_hash");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        assertDoesNotThrow(() -> authService.register(registerRequest));

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void should_ThrowException_When_UsernameIsAlreadyTaken() {
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(registerRequest));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void should_ThrowException_When_EmailIsAlreadyTaken() {
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(registerRequest));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void should_ReturnToken_When_CredentialsAreValid() {
        when(userRepository.findByUsername(loginRequest.getUsername())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateToken(testUser.getUsername(), "USER")).thenReturn("mocked-jwt-token");

        String token = authService.login(loginRequest);

        assertNotNull(token);
        assertEquals("mocked-jwt-token", token);
    }

    @Test
    void should_ThrowException_When_UserDoesNotExistInDatabase() {
        when(userRepository.findByUsername(loginRequest.getUsername())).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void should_ThrowException_When_PasswordIsIncorrect() {
        when(userRepository.findByUsername(loginRequest.getUsername())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
    }

    @Test
    void should_CompleteSuccessfully_When_UserExistsInDatabase() {
        when(userRepository.existsByUsername("test-user")).thenReturn(true);

        assertDoesNotThrow(() -> authService.verifyUserExists("test-user"));
    }

    @Test
    void should_ThrowException_When_UserDoesNotExistDuringVerification() {
        when(userRepository.existsByUsername("username-with-bad-token")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.verifyUserExists("username-with-bad-token"));
    }
}
