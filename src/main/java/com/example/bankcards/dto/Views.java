package com.example.bankcards.dto;

public class Views {
    public static class Public {}
    public static class User extends Public {} // наследует Public
    public static class Admin extends User {} // наследует Internal и Public
}