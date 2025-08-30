package com.example.bankcards.security.impl;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.exception.UnauthorizedException;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.security.SecurityService;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.PasswordEncoder;
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
        String password = request.getPassword();
        String login = request.getLogin();
        User user = userService.findByLogin(login);

        if (user == null) {
            throw new NotFoundException("User with login " + login + " not found");
        } else if (!PasswordEncoder.matches(password, user.getPassword())) {
            throw new UnauthorizedException("Wrong password");
        }

        String jwt = jwtService.generateToken(user);
        return new AuthResponse(jwt);
    }

    @Override
    public AuthResponse signup(AuthRequest request) {
        String login = request.getLogin();
        String encodedPassword = PasswordEncoder.encodePassword(request.getPassword());

        if (userService.findByLogin(login) != null) {
            throw new UnauthorizedException("User with such login already exists");
        }

        User newUser = new User(login, encodedPassword, User.Role.USER);
        userService.save(newUser);
        String jwt = jwtService.generateToken(newUser);
        return new AuthResponse(jwt);
    }
}
