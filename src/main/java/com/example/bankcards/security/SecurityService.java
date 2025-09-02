package com.example.bankcards.security;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.JwtResponse;

public interface SecurityService {
    JwtResponse signin(AuthRequest authRequest);

    JwtResponse signup(AuthRequest authRequest);
}
