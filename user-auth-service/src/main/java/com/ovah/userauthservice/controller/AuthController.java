package com.ovah.userauthservice.controller;

import com.ovah.userauthservice.DTO.AuthResponse;
import com.ovah.userauthservice.DTO.LoginRequest;
import com.ovah.userauthservice.DTO.RegisterRequest;
import com.ovah.userauthservice.model.User;
import com.ovah.userauthservice.service.AuthService;
import com.ovah.userauthservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

//@Tag()
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }
}

