package com.example.bankcards.controller;

import com.example.bankcards.dto.AdminRequest;
import com.example.bankcards.dto.AuthResponse;

public interface UserController {
    AuthResponse requestAdmin(AdminRequest request);
}
