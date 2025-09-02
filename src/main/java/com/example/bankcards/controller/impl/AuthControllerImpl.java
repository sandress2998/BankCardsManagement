package com.example.bankcards.controller.impl;

import com.example.bankcards.controller.AuthController;
import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.JwtResponse;
import com.example.bankcards.security.SecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication API", description = "API для регистрации и аутентификации пользователей")
@RestController
@RequestMapping("/api/auth")
public class AuthControllerImpl implements AuthController {

    private final SecurityService securityService;

    public AuthControllerImpl(SecurityService securityService) {
        this.securityService = securityService;
    }

    @Operation(
        summary = "Аутентификация пользователя (вход)",
        description = "Проверяет логин и пароль, возвращает JWT токен при успешной аутентификации.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Успешный вход, возвращен JWT",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неверный логин или пароль"),
            @ApiResponse(responseCode = "404", description = "Пользователь с указанным логином не найден")
        }
    )
    @PostMapping("/signin")
    @Override
    public JwtResponse signin(@RequestBody AuthRequest request) {
        return securityService.signin(request);
    }

    @Operation(
        summary = "Регистрация нового пользователя",
        description = "Создает нового пользователя с ролью USER, возвращает JWT при успешной регистрации.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Успешная регистрация, возвращен JWT",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "401", description = "Нарушены условия регистрации (например, логин слишком длинный или логин уже существует)")
        }
    )
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public JwtResponse signup(@RequestBody AuthRequest request) {
        return securityService.signup(request);
    }
}

/*
@RestController
@RequestMapping("/api/auth")
public class AuthControllerImpl implements AuthController {
    private final SecurityService securityService;

    public AuthControllerImpl(SecurityService securityService) {
        this.securityService = securityService;
    }

    @PostMapping("/signin")
    @Override
    public JwtResponse signin(@RequestBody AuthRequest request) {
        return securityService.signin(request);
    }

    @PostMapping("/signup")
    @Override
    public JwtResponse signup(@RequestBody AuthRequest request) {
        return securityService.signup(request);
    }
}
*/