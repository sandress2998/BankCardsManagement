package com.example.bankcards.controller;

import com.example.bankcards.dto.AdminRequest;
import com.example.bankcards.dto.JwtResponse;
import com.example.bankcards.dto.UserInfoResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@EnableAutoConfiguration(exclude = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc
@Import(UserControllerImplTest.TestSecurityConfig.class)
public class UserControllerImplTest {

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        @Primary
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Autowired
    private JwtService jwtService;  // используем реальный бин

    private AdminRequest validAdminRequest;
    private JwtResponse jwtResponse;
    private UserInfoResponse userInfoResponse;
    private List<UserInfoResponse> usersList;
    private UUID testUserId;

    @BeforeEach
    void setup() {
        testUserId = UUID.randomUUID();
        validAdminRequest = new AdminRequest("admin-secret");
        jwtResponse = new JwtResponse("mock-jwt-token");
        userInfoResponse = new UserInfoResponse(
                testUserId,
                "testuser",
                User.Role.USER
        );
        usersList = Arrays.asList(
                new UserInfoResponse(UUID.randomUUID(), "user1", User.Role.USER),
                new UserInfoResponse(UUID.randomUUID(), "user2", User.Role.ADMIN),
                new UserInfoResponse(UUID.randomUUID(), "user3", User.Role.USER)
        );
    }

    // Вспомогательный метод генерации JWT для тестов
    private String generateJwtToken(String username, User.Role role) {
        User user = new User(username, "password", role);
        user.setId(UUID.randomUUID());
        // В зависимости от реализации JwtService, реализуйте генерацию токена
        // Например, если JwtService имеет метод generateTokenByUsernameRoles
        return jwtService.generateToken(user);
    }

    // Тесты для PATCH /api/user/admin
    @Test
    void requestAdmin_ReturnsJwt_OnValidSecret() throws Exception {
        when(userService.requestAdmin(any(AdminRequest.class))).thenReturn(jwtResponse);

        String token = generateJwtToken("testuser", User.Role.USER);

        mockMvc.perform(patch("/api/user/admin")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAdminRequest))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.jwt").value("mock-jwt-token"));

        verify(userService).requestAdmin(any(AdminRequest.class));
    }

    @Test
    void requestAdmin_ReturnsUnauthorized_OnInvalidSecret() throws Exception {
        when(userService.requestAdmin(any(AdminRequest.class)))
                .thenThrow(new com.example.bankcards.exception.UnauthorizedException("Invalid admin secret"));

        String token = generateJwtToken("testuser", User.Role.USER);

        mockMvc.perform(patch("/api/user/admin")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAdminRequest))
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requestAdmin_ReturnsUnauthorized_OnUnauthenticatedUser() throws Exception {
        mockMvc.perform(patch("/api/user/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAdminRequest))
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    // Тесты для GET /api/user/me
    @Test
    void getMe_ReturnsUserInfo_OnAuthenticatedUser() throws Exception {
        when(userService.getUserInfoById(any(UUID.class))).thenReturn(userInfoResponse);

        String token = generateJwtToken("550e8400-e29b-41d4-a716-446655440000", User.Role.USER);

        mockMvc.perform(get("/api/user/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userInfoResponse.id().toString()))
                .andExpect(jsonPath("$.login").value(userInfoResponse.login()))
                .andExpect(jsonPath("$.role").value(userInfoResponse.role()));

        verify(userService).getUserInfoById(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
    }

    @Test
    void getMe_ReturnsUnauthorized_OnUnauthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMe_ReturnsNotFound_WhenUserNotFound() throws Exception {
        when(userService.getUserInfoById(any(UUID.class)))
                .thenThrow(new com.example.bankcards.exception.NotFoundException("User not found"));

        String token = generateJwtToken("550e8400-e29b-41d4-a716-446655440000", User.Role.USER);

        mockMvc.perform(get("/api/user/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    // Тесты для GET /api/user
    @Test
    void getAll_ReturnsUsersList_OnAdminUser() throws Exception {
        when(userService.getAll(0, 3)).thenReturn(usersList);

        String token = generateJwtToken("adminuser", User.Role.ADMIN);

        mockMvc.perform(get("/api/user")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .param("page", "0")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[1].username").value("user2"))
                .andExpect(jsonPath("$[2].username").value("user3"));

        verify(userService).getAll(0, 3);
    }

    @Test
    void getAll_UsesDefaultParameters_WhenNotProvided() throws Exception {
        when(userService.getAll(0, 3)).thenReturn(usersList);

        String token = generateJwtToken("adminuser", User.Role.ADMIN);

        mockMvc.perform(get("/api/user")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(userService).getAll(0, 3);
    }

    @Test
    void getAll_UsesCustomParameters_WhenProvided() throws Exception {
        when(userService.getAll(1, 5)).thenReturn(Collections.emptyList());

        String token = generateJwtToken("adminuser", User.Role.ADMIN);

        mockMvc.perform(get("/api/user")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(userService).getAll(1, 5);
    }

    @Test
    void getAll_ReturnsForbidden_OnUserRole() throws Exception {
        String token = generateJwtToken("normaluser", User.Role.USER);

        mockMvc.perform(get("/api/user")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());

        verify(userService, never()).getAll(anyInt(), anyInt());
    }

    @Test
    void getAll_ReturnsUnauthorized_OnUnauthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/user"))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).getAll(anyInt(), anyInt());
    }

    @Test
    void getAll_ReturnsEmptyList_WhenNoUsersFound() throws Exception {
        when(userService.getAll(0, 3)).thenReturn(Collections.emptyList());

        String token = generateJwtToken("adminuser", User.Role.ADMIN);

        mockMvc.perform(get("/api/user")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }
}

