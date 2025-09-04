package com.example.bankcards.dto;

import com.example.bankcards.entity.User;

import java.util.UUID;

public record UserAuthInfo(UUID id, User.Role role) {}