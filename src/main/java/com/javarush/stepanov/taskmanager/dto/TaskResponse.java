package com.javarush.stepanov.taskmanager.dto;

import java.time.LocalDateTime;

import com.javarush.stepanov.taskmanager.model.enums.TaskStatus;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class TaskResponse {
    private Long id;

    private String title;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadline;

    private TaskStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
