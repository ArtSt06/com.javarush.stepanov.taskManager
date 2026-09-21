package com.javarush.stepanov.taskmanager.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.javarush.stepanov.taskmanager.dto.AuthResponse;
import com.javarush.stepanov.taskmanager.dto.LoginRequest;
import com.javarush.stepanov.taskmanager.dto.RegisterRequest;
import com.javarush.stepanov.taskmanager.service.AuthService;
import com.javarush.stepanov.taskmanager.configuration.constants.OpenApiConstants;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = OpenApiConstants.AUTH_TAG)
public class AuthController {

    private final AuthService authService;

    @Operation(summary = OpenApiConstants.REGISTER_SUMMARY, description = OpenApiConstants.REGISTER_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = OpenApiConstants.STATUS_201_DESCRIPTION),
            @ApiResponse(responseCode = "400", description = OpenApiConstants.STATUS_400_DESCRIPTION),
            @ApiResponse(responseCode = "409", description = OpenApiConstants.STATUS_409_DESCRIPTION)
    })
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
    }

    @Operation(summary = OpenApiConstants.LOGIN_SUMMARY, description = OpenApiConstants.LOGIN_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = OpenApiConstants.STATUS_200_DESCRIPTION),
            @ApiResponse(responseCode = "401", description = OpenApiConstants.STATUS_401_DESCRIPTION)
    })
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request);
        return new AuthResponse(token);
    }
}
