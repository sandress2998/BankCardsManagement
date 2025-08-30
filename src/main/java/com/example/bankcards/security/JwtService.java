package com.example.bankcards.security;

import com.example.bankcards.entity.User;

public interface JwtService {
    String generateToken(User user);

    User validateToken(String token);
}