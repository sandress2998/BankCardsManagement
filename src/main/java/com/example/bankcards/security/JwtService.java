package com.example.bankcards.security;

import com.example.bankcards.dto.UserAuthInfo;
import com.example.bankcards.entity.User;

public interface JwtService {
    String generateToken(User user);

    UserAuthInfo extractUserAuthInfo(String token);

    String changeRoleInJwt(String token, User.Role role);
}