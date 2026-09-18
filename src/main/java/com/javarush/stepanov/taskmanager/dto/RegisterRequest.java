package com.javarush.stepanov.taskmanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Имя пользователя обязательно для заполнения")
    @Size(min = 3, max = 32, message = "Имя пользователя должно быть от 3 до 32 символов")
    private String username;

    @NotBlank(message = "Email обязателен для заполнения")
    @Email(message = "Некорректный формат Email")
    private String email;

    @NotBlank(message = "Пароль обязателен для заполнения")
    @Size(min = 8, max = 128, message = "Пароль должен быть не менее 8 символов")
    private String password;
}
