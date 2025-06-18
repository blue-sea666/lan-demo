package com.example.demo.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @GetMapping("/info")
    @RequiresAuthentication
    public Map<String, Object> userInfo() {
        Subject subject = SecurityUtils.getSubject();
        String username = (String) subject.getPrincipal();
        return Map.of("username", username, "message", "用户信息");
    }

    @GetMapping("/admin")
    @RequiresRoles("admin")
    public String adminOnly() {
        return "管理员专属区域";
    }

    @GetMapping("/data")
    @RequiresPermissions("data:view")
    public String viewData() {
        return "敏感数据内容";
    }

    @GetMapping("/manage")
    @RequiresPermissions("user:manage")
    public String manageUsers() {
        return "用户管理功能";
    }
}