package com.example.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/appInfo")
public class AppInfoController {
    @RequestMapping("/about")
    public String about() {
        return "Hello World";
    }
}
