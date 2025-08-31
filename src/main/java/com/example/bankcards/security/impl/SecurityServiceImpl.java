package com.example.bankcards.security.impl;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.exception.UnauthorizedException;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.security.SecurityService;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.BCryptEncoder;
import org.springframework.stereotype.Service;

@Service
public class SecurityServiceImpl implements SecurityService {

    private final JwtService jwtService;
    private final UserService userService;

    public SecurityServiceImpl(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    public AuthResponse signin(AuthRequest request) {
        String password = request.password();
        String login = request.login();
        User user = userService.findByLogin(login);

        if (user == null) {
            throw new NotFoundException("User with login " + login + " not found");
        } else if (!BCryptEncoder.matches(password, user.getPassword())) {
            throw new UnauthorizedException("Wrong password");
        }

        String jwt = jwtService.generateToken(user);
        return new AuthResponse(jwt);
    }

    @Override
    public AuthResponse signup(AuthRequest request) {
        String login = request.login();
        if (login.length() > 100) {
            throw new UnauthorizedException("Login is too long. Login length must be less than 100 characters");
        }

        String encodedPassword = BCryptEncoder.encodePassword(request.password());

        if (userService.findByLogin(login) != null) {
            throw new UnauthorizedException("User with such login already exists");
        }

        User newUser = new User(login, encodedPassword, User.Role.USER);
        userService.save(newUser);
        String jwt = jwtService.generateToken(newUser);
        return new AuthResponse(jwt);
    }
}
