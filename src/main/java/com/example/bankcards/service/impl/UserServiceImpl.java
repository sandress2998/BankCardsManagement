package com.example.bankcards.service.impl;

import com.example.bankcards.dto.AdminRequest;
import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.dto.UserInfoResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.exception.UnauthorizedException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.BCryptEncoder;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtService jwtService;

    @Value("${security.admin.secret}")
    String hashedSecretForAdmin;

    @Override
    public User findByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    @Transactional
    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    /** Запрос на получение статуса ADMIN */
    @Transactional
    @Override
    public AuthResponse requestAdmin(AdminRequest request) {
        Authentication authData = getAuthData();
        String id = authData.getName();
        String jwt = authData.getCredentials().toString();
        User user = userRepository.findUserById(UUID.fromString(id));

        if (user == null) {
            throw new NotFoundException("User not found");
        }

        if (BCryptEncoder.matches(request.secret(), hashedSecretForAdmin)) {
            userRepository.updateRole(UUID.fromString(id), User.Role.ADMIN);

            String updatedJwt = jwtService.changeRoleInJwt(jwt, User.Role.ADMIN);
            return new AuthResponse(updatedJwt);
        } else {
            throw new UnauthorizedException("Wrong password");
        }
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<UserInfoResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userRepository.findAll(pageable);
        List<UserInfoResponse> userInfoResponses = new ArrayList<>();
        for (User user : users.getContent()) {
            userInfoResponses.add(new UserInfoResponse(user.getId(), user.getLogin(), user.getRole()));
        }

        return userInfoResponses;
    }

    @Override
    public User findById(UUID id) {
        return userRepository.findUserById(id);
    }

    /** Извлечение authentication (data) из SecurityContext */
    private Authentication getAuthData() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated");
        }

        return auth;
    }
}
