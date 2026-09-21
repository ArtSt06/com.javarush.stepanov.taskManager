package com.javarush.stepanov.taskmanager;

import java.time.LocalDateTime;
import java.util.Map;

import com.javarush.stepanov.taskmanager.dto.LoginRequest;
import com.javarush.stepanov.taskmanager.dto.RegisterRequest;
import com.javarush.stepanov.taskmanager.dto.TaskRequest;
import com.javarush.stepanov.taskmanager.model.entity.User;
import com.javarush.stepanov.taskmanager.model.enums.Role;
import com.javarush.stepanov.taskmanager.model.enums.TaskStatus;
import com.javarush.stepanov.taskmanager.model.repository.UserRepository;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaskManagerApplicationIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String password = "password";

    private void createUser(String username, String email, Role role) {
        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.saveAndFlush(user);
    }

    private RegisterRequest buildRegisterRequest(String username, String email) {
        RegisterRequest register = new RegisterRequest();
        register.setUsername(username);
        register.setEmail(email);
        register.setPassword(password);
        return register;
    }

    private TaskRequest buildTaskRequest(String title, TaskStatus status) {
        TaskRequest request = new TaskRequest();
        request.setTitle(title);
        request.setDescription("Task Description");
        request.setDeadline(LocalDateTime.now().plusDays(3));
        request.setStatus(status);
        return request;
    }

    private ResultActions performRequest(MockHttpServletRequestBuilder requestBuilder, String token, Object body) throws Exception {
        if (token != null) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }
        if (body != null) {
            requestBuilder.contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body));
        }
        return mockMvc.perform(requestBuilder);
    }

    private String obtainAccessToken(String username) throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        MvcResult result = performRequest(post("/api/auth/login"), null, loginRequest)
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Map<String, String> tokenMap = objectMapper.readValue(responseBody, Map.class);
        return tokenMap.get("token");
    }

    private ResultActions registerUserViaApi(String username, String email) throws Exception {
        RegisterRequest request = buildRegisterRequest(username, email);
        return performRequest(post("/api/auth/register"), null, request);
    }

    private Integer createTaskViaApi(String title, String token) throws Exception {
        TaskRequest request = buildTaskRequest(title, TaskStatus.TODO);

        MvcResult result = performRequest(post("/api/tasks"), token, request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value(title))
                .andReturn();

        Map<String, Object> taskMap = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
        return (Integer) taskMap.get("id");
    }

    @Nested
    class AuthFlowIT {
        @Test
        void should_AuthenticateUser_When_CredentialsAreCorrect() throws Exception {
            registerUserViaApi("auth-user", "auth@example.com")
                    .andExpect(status().isCreated());

            LoginRequest login = new LoginRequest();
            login.setUsername("auth-user");
            login.setPassword(password);

            performRequest(post("/api/auth/login"), null, login)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").exists());
        }

        @Test
        void should_ReturnConflict_When_UserIsAlreadyRegistered() throws Exception {
            createUser("duplicate-user", "existing@example.com", Role.USER);

            registerUserViaApi("duplicate-user", "new-email@example.com")
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("Conflict"));

            registerUserViaApi("duplicate-user-2", "existing@example.com")
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("Conflict"));
        }
    }

    @Nested
    class UserTaskCrudFlowIT {
        @Test
        void should_PerformFullCrudLifecycle_On_UserTasks() throws Exception {
            createUser("crud-user", "crud@example.com", Role.USER);
            String token = obtainAccessToken("crud-user");

            Integer taskId = createTaskViaApi("Task Title", token);

            performRequest(get("/api/tasks/" + taskId), token, null)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(taskId))
                    .andExpect(jsonPath("$.title").value("Task Title"));

            TaskRequest updateRequest = buildTaskRequest("Updated Title", TaskStatus.IN_PROGRESS);

            performRequest(put("/api/tasks/" + taskId), token, updateRequest)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Updated Title"))
                    .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

            performRequest(delete("/api/tasks/" + taskId), token, null)
                    .andExpect(status().isNoContent());

            performRequest(get("/api/tasks/" + taskId), token, null)
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class RoleBasedAccessControlIT {
        @Test
        void should_EnforceSecurityBoundaries_Between_UsersAndAdmin() throws Exception {
            createUser("user-a", "a@example.com", Role.USER);
            createUser("user-b", "b@example.com", Role.USER);
            createUser("admin-user", "admin@example.com", Role.ADMIN);

            String tokenA = obtainAccessToken("user-a");
            String tokenB = obtainAccessToken("user-b");
            String tokenAdmin = obtainAccessToken("admin-user");

            Integer taskAId = createTaskViaApi("User A Task", tokenA);

            performRequest(get("/api/tasks/" + taskAId), null, null)
                    .andExpect(status().isUnauthorized());

            performRequest(get("/api/tasks/" + taskAId), tokenB, null)
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Forbidden"));

            performRequest(get("/api/tasks/" + taskAId), tokenAdmin, null)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("User A Task"));
        }
    }
}