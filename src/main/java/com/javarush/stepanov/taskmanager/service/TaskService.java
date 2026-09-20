package com.javarush.stepanov.taskmanager.service;

import java.util.List;
import java.util.stream.Collectors;

import com.javarush.stepanov.taskmanager.dto.TaskRequest;
import com.javarush.stepanov.taskmanager.dto.TaskResponse;
import com.javarush.stepanov.taskmanager.model.entity.Task;
import com.javarush.stepanov.taskmanager.model.entity.User;
import com.javarush.stepanov.taskmanager.model.repository.TaskRepository;
import com.javarush.stepanov.taskmanager.model.repository.UserRepository;
import com.javarush.stepanov.taskmanager.exception.AccessDeniedException;
import com.javarush.stepanov.taskmanager.exception.ErrorMessageConstants;
import com.javarush.stepanov.taskmanager.exception.ResourceNotFoundException;

import org.springframework.security.core.context.SecurityContextHolder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    private User getCurrentOwner() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessageConstants.USER_NOT_FOUND));
    }

    private Task getTaskAndVerifyOwner(Long id, String accessDeniedMessage) {
        User currentOwner = getCurrentOwner();

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessageConstants.TASK_NOT_FOUND));

        if (!task.getOwner().getId().equals(currentOwner.getId())) {
            throw new AccessDeniedException(accessDeniedMessage);
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

        log.info("Создание задачи с ID: {}", savedTask.getId());
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
        Task task = getTaskAndVerifyOwner(id, ErrorMessageConstants.ACCESS_DENIED_VIEW);

        return mapToResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request) {
        Task task = getTaskAndVerifyOwner(id, ErrorMessageConstants.ACCESS_DENIED_UPDATE);

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDeadline(request.getDeadline());
        task.setStatus(request.getStatus());

        Task updatedTask = taskRepository.save(task);

        log.info("Обновление задачи с ID: {}", id);
        return mapToResponse(updatedTask);
    }

    @Transactional
    public void deleteTask(Long id) {
        Task task = getTaskAndVerifyOwner(id, ErrorMessageConstants.ACCESS_DENIED_DELETE);

        log.info("Удаление задачи с ID: {}", id);
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
