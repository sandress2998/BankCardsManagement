package com.example.bankcards.controller;

import com.example.bankcards.dto.AdminRequest;
import com.example.bankcards.dto.JwtResponse;
import com.example.bankcards.dto.UserInfoResponse;
import com.example.bankcards.entity.User;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

public interface UserController {
    JwtResponse requestAdmin(AdminRequest request);

    UserInfoResponse getMe();

    List<UserInfoResponse> getAll(int page, int size);
}
