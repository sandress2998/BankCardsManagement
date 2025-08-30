package com.example.bankcards.service;

import com.example.bankcards.dto.AdminRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

public interface UserService {

    User findByLogin(String login);

    User save(User user);

    void requestAdmin(AdminRequest secret);
}
