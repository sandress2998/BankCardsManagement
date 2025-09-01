package com.example.bankcards.dto;

import com.example.bankcards.entity.User;

import java.util.UUID;

public record UserInfoResponse (UUID id, String login, User.Role role) {}
