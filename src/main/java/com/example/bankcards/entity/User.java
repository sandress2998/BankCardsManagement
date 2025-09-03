package com.example.bankcards.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity(name = "user_info")
public class User {
    public static final int LOGIN_MAX_LENGTH = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, length = LOGIN_MAX_LENGTH)
    private String login;

    @Column(nullable = false, length = 60)
    private String password;

    @Column(nullable = false, length = 20)
    private Role role;

    @JsonBackReference
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Card> cards = new ArrayList<>();

    public User(String login, String password, Role role) {
        this.login = login;
        this.password = password;
        this.role = role;
    }

    public User() {}

    public UUID getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public void setId(UUID id) { this.id = id; }

    public static void validateUser(User user) {
        validateLogin(user.getLogin());
    }

    public static void validateLogin(String login) {
        if (login.length() > LOGIN_MAX_LENGTH) {
            throw new IllegalArgumentException("Login is too long: length must be less than or equal to " + LOGIN_MAX_LENGTH);
        }
    }

    public enum Role {
        USER, ADMIN
    }
}
