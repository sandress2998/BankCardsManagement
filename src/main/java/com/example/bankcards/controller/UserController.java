package com.example.bankcards.controller;

import com.example.bankcards.dto.AdminRequest;
import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.dto.UserInfoResponse;
import com.example.bankcards.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

public interface UserController {
    AuthResponse requestAdmin(AdminRequest request);

    User getMe();

    @GetMapping
    List<UserInfoResponse> getAll(int page, int size);
}
