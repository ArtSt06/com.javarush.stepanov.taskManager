package com.javarush.stepanov.taskmanager.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.javarush.stepanov.taskmanager.dto.TaskRequest;
import com.javarush.stepanov.taskmanager.dto.TaskResponse;
import com.javarush.stepanov.taskmanager.exception.AccessDeniedException;
import com.javarush.stepanov.taskmanager.exception.ResourceNotFoundException;
import com.javarush.stepanov.taskmanager.model.entity.*;
import com.javarush.stepanov.taskmanager.model.enums.*;
import com.javarush.stepanov.taskmanager.model.repository.*;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private TaskService taskService;

    private User currentUser;
    private User currentAdmin;
    private User otherUser;
    private Task testTask;
    private TaskRequest taskRequest;

    @BeforeEach
    void setUp() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        currentUser = User.builder()
                .id(1L)
                .username("test-user")
                .email("user@example.com")
                .role(Role.USER)
                .build();
        currentAdmin = User.builder()
                .id(2L)
                .username("test-admin")
                .email("admin@example.com")
                .role(Role.ADMIN).build();
        otherUser = User.builder()
                .id(3L)
                .username("another-test-user")
                .email("other@example.com")
                .role(Role.USER)
                .build();

        taskRequest = new TaskRequest();
        taskRequest.setTitle("Updated Task Title");
        taskRequest.setDescription("Updated Task Description");
        taskRequest.setDeadline(LocalDateTime.now().plusDays(2));
        taskRequest.setStatus(TaskStatus.IN_PROGRESS);

        testTask = Task.builder()
                .id(100L)
                .title("Initial Task Title")
                .description("Initial Task Description")
                .deadline(LocalDateTime.now().plusDays(1))
                .status(TaskStatus.TODO)
                .owner(currentUser)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    class OwnerScenarios {
        @BeforeEach
        void setUpOwnerContext() {
            when(authentication.getName()).thenReturn("test-user");
            when(userRepository.findByUsername("test-user")).thenReturn(Optional.of(currentUser));
        }

        @Test
        void should_CreateTask_When_RequestIsValid() {
            when(taskRepository.save(any(Task.class))).thenReturn(testTask);

            TaskResponse response = taskService.createTask(taskRequest);

            assertNotNull(response);
            assertEquals(100L, response.getId());
            verify(taskRepository, times(1)).save(any(Task.class));
        }

        @Test
        void should_ReturnAllTasks_When_UserHasTasks() {
            when(taskRepository.findByOwnerId(currentUser.getId())).thenReturn(List.of(testTask));

            List<TaskResponse> tasks = taskService.getAllTasks();

            assertNotNull(tasks);
            assertEquals(1, tasks.size());
        }

        @Test
        void should_ReturnTask_When_UserIsTheOwner() {
            when(taskRepository.findById(100L)).thenReturn(Optional.of(testTask));

            TaskResponse response = taskService.getTaskById(100L);

            assertNotNull(response);
            assertEquals(100L, response.getId());
        }

        @Test
        void should_UpdateTask_When_UserIsTheOwner() {
            when(taskRepository.findById(100L)).thenReturn(Optional.of(testTask));
            when(taskRepository.save(any(Task.class))).thenReturn(testTask);

            TaskResponse response = taskService.updateTask(100L, taskRequest);

            assertNotNull(response);
            assertEquals("Updated Task Title", testTask.getTitle());
            assertEquals(TaskStatus.IN_PROGRESS, testTask.getStatus());
            verify(taskRepository, times(1)).save(testTask);
        }

        @Test
        void should_DeleteTask_When_UserIsTheOwner() {
            when(taskRepository.findById(100L)).thenReturn(Optional.of(testTask));

            assertDoesNotThrow(() -> taskService.deleteTask(100L));

            verify(taskRepository, times(1)).delete(testTask);
        }
    }

    @Nested
    class UserSecurityBoundaries {
        @BeforeEach
        void setUpUserContext() {
            when(authentication.getName()).thenReturn("test-user");
            when(userRepository.findByUsername("test-user")).thenReturn(Optional.of(currentUser));
        }

        @Test
        void should_ThrowResourceNotFoundException_When_TaskDoesNotExist() {
            when(taskRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> taskService.getTaskById(999L));
        }

        @Test
        void should_ThrowAccessDeniedException_When_UserAttemptsToGetOtherUserTask() {
            testTask.setOwner(otherUser);

            when(taskRepository.findById(100L)).thenReturn(Optional.of(testTask));

            assertThrows(AccessDeniedException.class, () -> taskService.getTaskById(100L));
        }

        @Test
        void should_ThrowAccessDeniedException_When_UserAttemptsToUpdateOtherUserTask() {
            testTask.setOwner(otherUser);

            when(taskRepository.findById(100L)).thenReturn(Optional.of(testTask));

            assertThrows(AccessDeniedException.class, () -> taskService.updateTask(100L, taskRequest));
            verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        void should_ThrowAccessDeniedException_When_UserAttemptsToDeleteOtherUserTask() {
            testTask.setOwner(otherUser);

            when(taskRepository.findById(100L)).thenReturn(Optional.of(testTask));

            assertThrows(AccessDeniedException.class, () -> taskService.deleteTask(100L));
            verify(taskRepository, never()).delete(any(Task.class));
        }
    }

    @Nested
    class AdminPrivileges {
        @BeforeEach
        void setUpAdminContext() {
            testTask.setOwner(otherUser); // Для всей группы админа задача априори чужая
            when(authentication.getName()).thenReturn("test-admin");
            when(userRepository.findByUsername("test-admin")).thenReturn(Optional.of(currentAdmin));
            when(taskRepository.findById(100L)).thenReturn(Optional.of(testTask));
        }

        @Test
        void should_AllowAdminToGetOtherUserTask_When_RoleIsAdmin() {
            assertDoesNotThrow(() -> taskService.getTaskById(100L));
        }

        @Test
        void should_AllowAdminToUpdateOtherUserTask_When_RoleIsAdmin() {
            when(taskRepository.save(any(Task.class))).thenReturn(testTask);

            assertDoesNotThrow(() -> taskService.updateTask(100L, taskRequest));
            verify(taskRepository, times(1)).save(testTask);
        }

        @Test
        void should_AllowAdminToDeleteOtherUserTask_When_RoleIsAdmin() {
            assertDoesNotThrow(() -> taskService.deleteTask(100L));
            verify(taskRepository, times(1)).delete(testTask);
        }
    }
}
