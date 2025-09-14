package com.example.bankcards.controller;

import com.example.bankcards.dto.UserInfoResponse;

import java.util.List;

public interface AdminUserController {
    List<UserInfoResponse> getUsers(int page, int size);
}
