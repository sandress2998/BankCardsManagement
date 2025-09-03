package com.example.bankcards.controller;

import com.example.bankcards.dto.AdminRequest;
import com.example.bankcards.dto.JwtResponse;
import com.example.bankcards.dto.UserInfoResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.JwtFilter;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
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
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfig.class)
public class UserControllerImplTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private AdminRequest validAdminRequest;
    private JwtResponse jwtResponse;
    private UserInfoResponse userInfoResponse;
    private List<UserInfoResponse> usersList;

    private static final String userId = "6d2626cb-4b4f-4567-96a6-256d0002a4f8";

    @BeforeEach
    void setup() {
        validAdminRequest = new AdminRequest("admin-secret");
        jwtResponse = new JwtResponse("mock-jwt-token");
        userInfoResponse = new UserInfoResponse(
            UUID.fromString(userId),
            "testuser",
            User.Role.USER
        );
        usersList = Arrays.asList(
            new UserInfoResponse(UUID.randomUUID(), "user1", User.Role.USER),
            new UserInfoResponse(UUID.randomUUID(), "user2", User.Role.ADMIN),
            new UserInfoResponse(UUID.randomUUID(), "user3", User.Role.USER)
        );
    }

    // Тесты для PATCH /api/user/admin
    @Test
    @WithMockUser(username = userId, roles = {"USER"})
    void requestAdmin_ReturnsJwt_OnValidSecret() throws Exception {
        when(userService.requestAdmin(any(AdminRequest.class))).thenReturn(jwtResponse);

        mockMvc.perform(patch("/api/user/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAdminRequest))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.jwt").value("mock-jwt-token"));

        verify(userService).requestAdmin(any(AdminRequest.class));
    }

    @Test
    @WithMockUser(username = userId, roles = {"USER"})
    void requestAdmin_ReturnsAccessDenied_OnInvalidSecret() throws Exception {
        when(userService.requestAdmin(any(AdminRequest.class)))
            .thenThrow(new AccessDeniedException("Invalid admin secret"));

        mockMvc.perform(patch("/api/user/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAdminRequest))
                .with(csrf()))
            .andExpect(status().isForbidden());
    }

    // Тесты для GET /api/user/me
    @Test
    @WithMockUser(username = userId, roles = {"USER"})
    void getMe_ReturnsUserInfo_OnValidUser() throws Exception {
        // Мокаем вызов сервиса
        when(userService.getUserInfoById(any(UUID.class))).thenReturn(userInfoResponse);

        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userInfoResponse.id().toString()))
                .andExpect(jsonPath("$.login").value(userInfoResponse.login()))
                .andExpect(jsonPath("$.role").value(userInfoResponse.role().toString()));

        verify(userService).getUserInfoById(any(UUID.class));
    }

    @Test
    @WithMockUser(username = userId, roles = {"USER"})
    void getMe_ReturnsNotFound_WhenUserNotFound() throws Exception {
        when(userService.getUserInfoById(any(UUID.class)))
            .thenThrow(new com.example.bankcards.exception.NotFoundException("User not found"));

        mockMvc.perform(get("/api/user/me"))
            .andExpect(status().isNotFound());
    }

    // Тесты для GET /api/user
    @Test
    @WithMockUser(username = userId, roles = {"ADMIN"})
    void getAll_ReturnsUsersList_OnAdminUser() throws Exception {
        when(userService.getAll(0, 3)).thenReturn(usersList);

        mockMvc.perform(get("/api/user")
            .param("page", "0")
            .param("size", "3"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0].login").value("user1"))
            .andExpect(jsonPath("$[1].login").value("user2"))
            .andExpect(jsonPath("$[2].login").value("user3"));

        verify(userService).getAll(0, 3);
    }

    @Test
    @WithMockUser(username = userId, roles = {"ADMIN"})
    void getAll_UsesDefaultParameters_WhenNotProvided() throws Exception {
        when(userService.getAll(0, 3)).thenReturn(usersList);

        mockMvc.perform(get("/api/user"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(userService).getAll(0, 3);
    }

    @Test
    @WithMockUser(username = userId, roles = {"ADMIN"})
    void getAll_UsesCustomParameters_WhenProvided() throws Exception {
        when(userService.getAll(1, 5)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/user")
            .param("page", "1")
            .param("size", "5"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(0)));

        verify(userService).getAll(1, 5);
    }

    /*
    @Test
    @WithMockUser(username = userId, roles = {"USER"})
    void getAll_ReturnsForbidden_OnUserRole() throws Exception {
        mockMvc.perform(get("/api/user"))
            .andExpect(status().isForbidden());

        // Метод сервиса не должен вызываться при 403
        verify(userService, never()).getAll(anyInt(), anyInt());
    }
     */

    @Test
    @WithMockUser(username = userId, roles = {"ADMIN"})
    void getAll_ReturnsEmptyList_WhenNoUsersFound() throws Exception {
        when(userService.getAll(0, 3)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/user"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }
}

