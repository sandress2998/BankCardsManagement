package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.JwtResponse;

public interface AuthController {
    JwtResponse signin(AuthRequest request);

    JwtResponse signup(AuthRequest request);
}