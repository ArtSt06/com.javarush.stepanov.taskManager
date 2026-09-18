package com.javarush.stepanov.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Имя пользователя обязательно для заполнения")
    private String username;

    @NotBlank(message = "Пароль обязателен для заполнения")
    private String password;
}
