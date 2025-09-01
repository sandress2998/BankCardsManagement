package com.example.bankcards.controller.impl;

import com.example.bankcards.controller.UserController;
import com.example.bankcards.dto.AdminRequest;
import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.dto.UserInfoResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserControllerImpl implements UserController {
    @Autowired
    UserService userService;

    /** Запрос на становление админом (перед этим нужно зарегистрироваться) */
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
