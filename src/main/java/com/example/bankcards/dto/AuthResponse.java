package com.example.bankcards.dto;

public class AuthResponse {
    public AuthResponse(String jwt) {
        this.jwt = jwt;
    }

    public String jwt;
}
