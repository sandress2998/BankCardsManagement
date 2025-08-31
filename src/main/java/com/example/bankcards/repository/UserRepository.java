package com.example.bankcards.repository;

import com.example.bankcards.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    User findByLogin(String login);

    @Modifying
    @Query("UPDATE user_info u SET u.role = :role WHERE u.id = :id")
    void updateRole(@Param("id") UUID id, @Param("role") User.Role role);

    User findUserById(UUID id);
}
