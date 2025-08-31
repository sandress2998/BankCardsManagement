package com.example.bankcards.service;

import com.example.bankcards.dto.AdminRequest;
import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.entity.User;

import java.util.UUID;

public interface UserService {

    User findByLogin(String login);

    User findById(UUID id);

    User save(User user);

    AuthResponse requestAdmin(AdminRequest secret);
}
