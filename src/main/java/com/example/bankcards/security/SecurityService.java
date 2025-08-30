package com.example.bankcards.security;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.AuthResponse;

public interface SecurityService {
    AuthResponse signin(AuthRequest authRequest);

    AuthResponse signup(AuthRequest authRequest);
}
