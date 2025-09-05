package com.example.bankcards.service.impl;

import com.example.bankcards.dto.AdminRequest;
import com.example.bankcards.dto.JwtResponse;
import com.example.bankcards.dto.UserInfoResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.exception.UnauthorizedException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.BCryptEncoder;
import jakarta.transaction.Transactional;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    private final String hashedSecretForAdmin;

    UserRepository userRepository;
    JwtService jwtService;

    public UserServiceImpl(
        Environment env,
        UserRepository userRepository,
        JwtService jwtService
    ) {
        hashedSecretForAdmin = env.getProperty("security.admin.secret");;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Override
    public User findByLogin(String login) {
        User.validateLogin(login);
        User user = userRepository.findByLogin(login);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        return user;
    }

    @Override
    public void checkIfNotExistsByLogin(String login) {
        User.validateLogin(login);
        if (userRepository.existsByLogin(login)) {
            throw new BadRequestException("User with such login already exists");
        }
    }

    @Transactional
    @Override
    public User save(User user) {
        User.validateUser(user);
        return userRepository.save(user);
    }

    /** Запрос на получение статуса ADMIN */
    @Transactional
    @Override
    public JwtResponse requestAdmin(AdminRequest request) {
        System.out.println("secret: " + hashedSecretForAdmin);
        Authentication authData = getAuthData();
        UUID id = UUID.fromString(authData.getName());
        checkIsExistById(id);
        String jwt = authData.getCredentials().toString();

        if (BCryptEncoder.matches(request.secret(), hashedSecretForAdmin)) {
            userRepository.updateRole(id, User.Role.ADMIN);

            String updatedJwt = jwtService.changeRoleInJwt(jwt, User.Role.ADMIN);
            return new JwtResponse(updatedJwt);
        } else {
            throw new AccessDeniedException("Wrong password");
        }
    }

    @Override
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
    public UserInfoResponse getUserInfoById(UUID id) {
        User user = userRepository.findUserById(id);

        if (user == null) {
            throw new NotFoundException("User not found");
        }
        return new UserInfoResponse(user.getId(), user.getLogin(), user.getRole());
    }

    private void checkIsExistById(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User not found");
        }
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
