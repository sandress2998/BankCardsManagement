package com.example.bankcards.controller.impl;

import com.example.bankcards.controller.UserController;
import com.example.bankcards.dto.AdminRequest;
import com.example.bankcards.dto.JwtResponse;
import com.example.bankcards.dto.UserInfoResponse;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@Tag(name = "User API", description = "API для работы с пользователями")
@RestController
@RequestMapping("/api/user")
public class UserControllerImpl implements UserController {
    private final UserService userService;

    public UserControllerImpl(UserService userService) {
        this.userService = userService;
    }

    @Operation(
        summary = "Запрос на получение роли администратора",
        description = "Аутентифицированный пользователь может запросить роль ADMIN, передав секрет",
        responses = {
            @ApiResponse(responseCode = "200", description = "Роль успешно обновлена, возвращен JWT",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "401", description = "Ошибка аутентификации или неверный секрет")
        }
    )
    @PatchMapping("/admin")
    @Override
    public JwtResponse requestAdmin(@RequestBody AdminRequest secret) {
        return userService.requestAdmin(secret);
    }

    @Operation(
        summary = "Получить информацию текущего пользователя",
        description = "Возвращает данные пользователя, на основании аутентификации",
        responses = {
            @ApiResponse(responseCode = "200", description = "Данные пользователя успешно получены",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserInfoResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
        }
    )
    @GetMapping("/me")
    public UserInfoResponse getMe() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.findById(UUID.fromString(auth.getName()));
    }

    @Operation(
        summary = "Получить список пользователей с пагинацией",
        description = "Доступно только пользователю с ролью ADMIN",
        responses = {
            @ApiResponse(responseCode = "200", description = "Список пользователей",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserInfoResponse.class))),
            @ApiResponse(responseCode = "403", description = "Отказано в доступе"),
            @ApiResponse(responseCode = "401", description = "JWT отсутствует или невалидный")
        }
    )
    @GetMapping
    public List<UserInfoResponse> getAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "3") int size
    ) {
        return userService.getAll(page, size);
    }
}

/*
@RestController
@RequestMapping("/api/user")
public class UserControllerImpl implements UserController {
    @Autowired
    UserService userService;

    @PostMapping("/admin")
    @Override
    public AuthResponse requestAdmin(@RequestBody AdminRequest secret) {
        return userService.requestAdmin(secret);
    }

    @GetMapping("/me")
    public User getMe() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        return userService.findById(UUID.fromString(auth.getName()));
    }

    @GetMapping
    public List<UserInfoResponse> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size
    ) {
        return userService.getAll(page, size);
    }
}
*/