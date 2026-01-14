package com.subnex.auth.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class DevConroller {

    @GetMapping("/")
    public String welcome() {
        return "Welcome to the Auth Service!";
    }

    @GetMapping("/health")
    public String healthCheck() {
        return "Auth Service is up and running!";
    }
}