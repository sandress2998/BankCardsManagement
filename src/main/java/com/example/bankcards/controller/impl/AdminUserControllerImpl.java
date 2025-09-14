package com.example.bankcards.controller.impl;

import com.example.bankcards.controller.AdminUserController;
import com.example.bankcards.dto.UserInfoResponse;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Admin User API", description = "API для управления пользователями. Доступно только для администраторов.")
@SecurityRequirement(name = "JWT")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RestController
@RequestMapping("/admin/api/users")
@RequiredArgsConstructor
public class AdminUserControllerImpl implements AdminUserController {

    private final UserService userService;

    @Operation(
            summary = "Получить список пользователей с пагинацией",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Список пользователей",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserInfoResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Отказано в доступе"),
                    @ApiResponse(responseCode = "401", description = "JWT отсутствует или невалидный")
            }
    )
    @GetMapping
    @Override
    public List<UserInfoResponse> getUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "3") int size
    ) {
        return userService.getAll(page, size);
    }
}
