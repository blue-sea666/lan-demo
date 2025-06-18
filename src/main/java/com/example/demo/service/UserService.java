package com.example.demo.service;

import com.example.demo.entity.User;

public interface UserService {
    User findByUsername(String username);
    User login(String username, String password);
}