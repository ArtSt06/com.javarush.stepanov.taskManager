package com.javarush.stepanov.taskmanager.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.javarush.stepanov.taskmanager.dto.TaskRequest;
import com.javarush.stepanov.taskmanager.dto.TaskResponse;
import com.javarush.stepanov.taskmanager.service.TaskService;
import com.javarush.stepanov.taskmanager.configuration.constants.OpenApiConstants;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
@Tag(name = OpenApiConstants.TASK_TAG)
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = OpenApiConstants.CREATE_TASK_SUMMARY, description = OpenApiConstants.CREATE_TASK_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = OpenApiConstants.STATUS_201_DESCRIPTION),
            @ApiResponse(responseCode = "401", description = OpenApiConstants.STATUS_401_DESCRIPTION)
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@Valid @RequestBody TaskRequest request) {
        return taskService.createTask(request);
    }

    @Operation(summary = OpenApiConstants.GET_ALL_TASKS_SUMMARY, description = OpenApiConstants.GET_ALL_TASKS_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = OpenApiConstants.STATUS_200_DESCRIPTION),
            @ApiResponse(responseCode = "401", description = OpenApiConstants.STATUS_401_DESCRIPTION)
    })
    @GetMapping
    public List<TaskResponse> getAll() {
        return taskService.getAllTasks();
    }

    @Operation(summary = OpenApiConstants.GET_TASK_SUMMARY, description = OpenApiConstants.GET_TASK_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = OpenApiConstants.STATUS_200_DESCRIPTION),
            @ApiResponse(responseCode = "401", description = OpenApiConstants.STATUS_401_DESCRIPTION),
            @ApiResponse(responseCode = "403", description = OpenApiConstants.STATUS_403_DESCRIPTION),
            @ApiResponse(responseCode = "404", description = OpenApiConstants.STATUS_404_DESCRIPTION)
    })
    @GetMapping("/{id}")
    public TaskResponse getById(@PathVariable Long id) {
        return taskService.getTaskById(id);
    }

    @Operation(summary = OpenApiConstants.UPDATE_TASK_SUMMARY, description = OpenApiConstants.UPDATE_TASK_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = OpenApiConstants.STATUS_200_DESCRIPTION),
            @ApiResponse(responseCode = "401", description = OpenApiConstants.STATUS_401_DESCRIPTION),
            @ApiResponse(responseCode = "403", description = OpenApiConstants.STATUS_403_DESCRIPTION),
            @ApiResponse(responseCode = "404", description = OpenApiConstants.STATUS_404_DESCRIPTION)
    })
    @PutMapping("/{id}")
    public TaskResponse update(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        return taskService.updateTask(id, request);
    }

    @Operation(summary = OpenApiConstants.DELETE_TASK_SUMMARY, description = OpenApiConstants.DELETE_TASK_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = OpenApiConstants.STATUS_204_DESCRIPTION),
            @ApiResponse(responseCode = "401", description = OpenApiConstants.STATUS_401_DESCRIPTION),
            @ApiResponse(responseCode = "403", description = OpenApiConstants.STATUS_403_DESCRIPTION),
            @ApiResponse(responseCode = "404", description = OpenApiConstants.STATUS_404_DESCRIPTION)
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        taskService.deleteTask(id);
    }
}
