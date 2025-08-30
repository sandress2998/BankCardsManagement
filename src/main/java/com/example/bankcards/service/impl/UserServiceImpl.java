package com.example.bankcards.service.impl;

import com.example.bankcards.dto.AdminRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.exception.UnauthorizedException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.PasswordEncoder;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserRepository userRepository;

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
    public void requestAdmin(AdminRequest request) {
        String login = getLogin();
        User user = userRepository.findByLogin(login);

        if (user == null) {
            throw new NotFoundException("User not found");
        }

        if (PasswordEncoder.matches(request.getSecret(), hashedSecretForAdmin)) {
            userRepository.updateRole(login, User.Role.ADMIN);
        } else {
            throw new UnauthorizedException("Wrong password");
        }
    }

    /** Извлечение login из SecurityContext */
    private String getLogin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated");
        }

        String login = auth.getName();

        if (login == null) {
            throw new UnauthorizedException("Something went wrong");
        } else {
            return login;
        }
    }
}
