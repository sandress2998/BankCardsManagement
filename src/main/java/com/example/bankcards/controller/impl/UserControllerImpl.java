package com.example.bankcards.controller.impl;

import com.example.bankcards.controller.UserController;
import com.example.bankcards.dto.JwtResponse;
import com.example.bankcards.dto.RoleRequest;
import com.example.bankcards.dto.UserInfoResponse;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "User API", description = "API для работы с пользователями")
@SecurityRequirement(name = "JWT")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserControllerImpl implements UserController {

    private final UserService userService;

    @Operation(
        summary = "Запрос на получение роли администратора",
        description = "Аутентифицированный пользователь может запросить роль ADMIN, передав секрет",
        responses = {
            @ApiResponse(responseCode = "200", description = "Роль успешно обновлена, возвращен JWT",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "401", description = "Ошибка аутентификации или неверный секрет")
        }
    )
    @PostMapping("/role")
    @Override
    public JwtResponse requestRole(@RequestBody RoleRequest body) { return userService.requestRole(body); }

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
    @Override
    public UserInfoResponse getMe() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.getUserInfoById(UUID.fromString(auth.getName()));
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