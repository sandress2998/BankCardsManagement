package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.JwtResponse;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.security.SecurityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerImplTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SecurityService securityService;

    @MockBean
    private JwtService jwtService;

    private AuthRequest validRequest;
    private JwtResponse jwtResponse;

    @BeforeEach
    void setup() {
        validRequest = new AuthRequest("testuser", "password123");
        jwtResponse = new JwtResponse("mock-jwt-token");
    }

    @Test
    void signin_ReturnsJwt_OnSuccess() throws Exception {
        when(securityService.signin(any(AuthRequest.class))).thenReturn(jwtResponse);

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.jwt").value("mock-jwt-token"));
    }

    @Test
    void signin_ReturnsUnauthorized_OnInvalidCredentials() throws Exception {
        when(securityService.signin(any(AuthRequest.class)))
                .thenThrow(new com.example.bankcards.exception.UnauthorizedException("Invalid login or password"));

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void signin_ReturnsNotFound_WhenUserNotFound() throws Exception {
        when(securityService.signin(any(AuthRequest.class)))
                .thenThrow(new com.example.bankcards.exception.NotFoundException("User not found"));

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void signup_ReturnsCreatedJwt_OnSuccess() throws Exception {
        when(securityService.signup(any(AuthRequest.class))).thenReturn(jwtResponse);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.jwt").value("mock-jwt-token"));
    }

    @Test
    void signup_ReturnsUnauthorized_OnBadRequest() throws Exception {
        when(securityService.signup(any(AuthRequest.class)))
                .thenThrow(new com.example.bankcards.exception.UnauthorizedException("Login too long or already exists"));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnauthorized());
    }
}




/*
@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = AuthControllerImpl.class)
class AuthControllerImplTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthRequest validRequest;
    private JwtResponse jwtResponse;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public SecurityService securityService() {
            return Mockito.mock(SecurityService.class);
        }
    }

    @Autowired
    private SecurityService securityService;

    @BeforeEach
    void setup() {
        validRequest = new AuthRequest("testuser", "password123");
        jwtResponse = new JwtResponse("mock-jwt-token");
    }

    @Test
    void signin_ReturnsJwt_OnSuccess() throws Exception {
        when(securityService.signin(any(AuthRequest.class))).thenReturn(jwtResponse);

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.jwt").value("mock-jwt-token"));
    }

    @Test
    void signin_ReturnsUnauthorized_OnInvalidCredentials() throws Exception {
        when(securityService.signin(any(AuthRequest.class)))
                .thenThrow(new com.example.bankcards.exception.UnauthorizedException("Invalid login or password"));

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void signin_ReturnsNotFound_WhenUserNotFound() throws Exception {
        when(securityService.signin(any(AuthRequest.class)))
                .thenThrow(new com.example.bankcards.exception.NotFoundException("User not found"));

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void signup_ReturnsCreatedJwt_OnSuccess() throws Exception {
        when(securityService.signup(any(AuthRequest.class))).thenReturn(jwtResponse);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.jwt").value("mock-jwt-token"));
    }

    @Test
    void signup_ReturnsUnauthorized_OnBadRequest() throws Exception {
        when(securityService.signup(any(AuthRequest.class)))
                .thenThrow(new com.example.bankcards.exception.UnauthorizedException("Login too long or already exists"));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnauthorized());
    }
}
*/