package com.javarush.stepanov.taskmanager.service;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.javarush.stepanov.taskmanager.dto.TaskRequest;
import com.javarush.stepanov.taskmanager.dto.TaskResponse;
import com.javarush.stepanov.taskmanager.model.entity.Task;
import com.javarush.stepanov.taskmanager.model.entity.User;
import com.javarush.stepanov.taskmanager.model.repository.TaskRepository;
import com.javarush.stepanov.taskmanager.model.repository.UserRepository;

import org.springframework.security.core.context.SecurityContextHolder;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    private User getCurrentOwner() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Авторизованный пользователь не найден в базе данных"));
    }

    private Task getTaskAndVerifyOwner(
            Long id,
            Supplier<? extends RuntimeException> notFoundSupplier,
            Supplier<? extends RuntimeException> accessDeniedSupplier) {

        User currentOwner = getCurrentOwner();

        Task task = taskRepository.findById(id)
                .orElseThrow(notFoundSupplier);

        if (!task.getOwner().getId().equals(currentOwner.getId())) {
            throw accessDeniedSupplier.get();
        }

        return task;
    }

    @Transactional
    public TaskResponse createTask(TaskRequest request) {
        User currentOwner = getCurrentOwner();

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .deadline(request.getDeadline())
                .status(request.getStatus())
                .owner(currentOwner)
                .build();

        Task savedTask = taskRepository.save(task);
        return mapToResponse(savedTask);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasks() {
        User currentOwner = getCurrentOwner();
        return taskRepository.findByOwnerId(currentOwner.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {
        Task task = getTaskAndVerifyOwner(
                id,
                () -> new RuntimeException("Задача с таким ID не существует"),
                () -> new RuntimeException("Отсутствуют права на просмотр этой задачи")
        );

        return mapToResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request) {
        Task task = getTaskAndVerifyOwner(
                id,
                () -> new RuntimeException("Задача с таким ID не существует"),
                () -> new RuntimeException("Отсутствуют права на обновление этой задачи")
        );

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDeadline(request.getDeadline());
        task.setStatus(request.getStatus());

        Task updatedTask = taskRepository.save(task);
        return mapToResponse(updatedTask);
    }

    @Transactional
    public void deleteTask(Long id) {
        Task task = getTaskAndVerifyOwner(
                id,
                () -> new RuntimeException("Задача с таким ID не существует"),
                () -> new RuntimeException("Отсутствуют права на удаление этой задачи")
        );

        taskRepository.delete(task);
    }

    private TaskResponse mapToResponse(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setDeadline(task.getDeadline());
        response.setStatus(task.getStatus());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());
        return response;
    }
}
