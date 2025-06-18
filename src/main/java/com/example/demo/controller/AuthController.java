package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    public AuthController(UserService userService,
                          JwtUtil jwtUtil,
                          RedisTemplate<String, String> redisTemplate) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User users) {
        String username = users.getUsername();
        String password = users.getPassword();
        System.out.println(username);
        User user = userService.login(username, password);
        if (user == null) {
            return Map.of("code", 401, "message", "用户名或密码错误");
        }

        // 生成JWT
        String token = jwtUtil.generateToken(username);

        // 存储到Redis
        String redisKey = "token_" + username;
        redisTemplate.opsForValue().set(
                redisKey,
                token,
                jwtUtil.getExpire() + jwtUtil.getRefresh(),
                TimeUnit.SECONDS
        );

        return Map.of(
                "code", 200,
                "token", token,
                "user", Map.of(
                        "username", username,
                        "role", user.getRole()
                )
        );
    }

    @PostMapping("/logout")
    @RequiresAuthentication
    public Map<String, Object> logout() {
        Subject subject = SecurityUtils.getSubject();
        String username = (String) subject.getPrincipal();

        // 从Redis中删除token
        redisTemplate.delete("token_" + username);

        // 登出
        subject.logout();

        return Map.of("code", 200, "message", "登出成功");
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        // 简单的注册逻辑
        if (userService.findByUsername(user.getUsername()) != null) {
            return Map.of("code", 400, "message", "用户名已存在");
        }

        // 设置默认角色
        user.setRole("user");
        // 实际项目中应对密码加密
        // user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 保存用户（需要实现save方法）
        // userService.save(user);

        return Map.of("code", 200, "message", "注册成功");
    }
}