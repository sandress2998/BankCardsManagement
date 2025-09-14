package com.example.bankcards.security.impl;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.JwtResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UnauthorizedException;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.security.SecurityService;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.security.BCryptEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityServiceImpl implements SecurityService {

    private final JwtService jwtService;
    private final UserService userService;

    @Override
    public JwtResponse signin(AuthRequest request) {
        String password = request.password();
        String login = request.login();
        User user = userService.findByLogin(login);

        if (!BCryptEncoder.matches(password, user.getPassword())) {
            throw new UnauthorizedException("Wrong password");
        }

        String jwt = jwtService.generateToken(user);
        return new JwtResponse(jwt);
    }

    @Override
    public JwtResponse signup(AuthRequest request) {
        String login = request.login();
        if (login.length() > 100) {
            throw new UnauthorizedException("Login is too long. Login length must be less than 100 characters");
        }

        String encodedPassword = BCryptEncoder.encodePassword(request.password());

        userService.checkIfNotExistsByLogin(login);

        User newUser = new User(login, encodedPassword, User.Role.USER);
        userService.save(newUser);
        String jwt = jwtService.generateToken(newUser);
        return new JwtResponse(jwt);
    }
}
