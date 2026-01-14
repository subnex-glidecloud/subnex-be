package com.subnex.dev.controller;

import com.subnex.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@GetMapping("/")
@RestController

public class DevConroller {

    private final AuthService authService;

    @GetMapping("/")
    public String welcome() {
        return "Welcome to the Auth Service!";
    }

    @GetMapping("/health")
    public String healthCheck() {
        return "Auth Service is up and running!";
    }
}