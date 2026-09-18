package com.javarush.stepanov.taskmanager.dto;

import java.time.LocalDateTime;

import com.javarush.stepanov.taskmanager.model.enums.TaskStatus;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TaskRequest {

    @NotBlank(message = "Заголовок задачи не может быть пустым")
    @Size(min = 3, max = 100, message = "Заголовок задачи должен быть от 3 до 100 символов")
    private String title;

    @Size(max = 1000, message = "Описание задачи не может превышать 1000 символов")
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadline;

    @NotNull(message = "Статус задачи обязателен")
    private TaskStatus status;
}
