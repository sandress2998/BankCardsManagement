package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.AuthResponse;

public interface AuthController {
    AuthResponse signin(AuthRequest request);

    AuthResponse signup(AuthRequest request);
}