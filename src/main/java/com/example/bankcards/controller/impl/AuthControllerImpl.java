package com.example.bankcards.controller.impl;

import com.example.bankcards.controller.AuthController;
import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthControllerImpl implements AuthController {
    @Autowired
    SecurityService securityService;

    @PostMapping("/signin")
    @Override
    public AuthResponse signin(@RequestBody AuthRequest request) {
        return securityService.signin(request);
    }

    @PostMapping("/signup")
    @Override
    public AuthResponse signup(@RequestBody AuthRequest request) {
        return securityService.signup(request);
    }
}
