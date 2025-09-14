package com.example.bankcards.controller;

import com.example.bankcards.dto.RoleRequest;
import com.example.bankcards.dto.JwtResponse;
import com.example.bankcards.dto.UserInfoResponse;

import java.util.List;

public interface UserController {
    JwtResponse requestRole(RoleRequest request);

    UserInfoResponse getMe();
}
