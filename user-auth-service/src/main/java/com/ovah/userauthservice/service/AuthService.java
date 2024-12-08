package com.ovah.userauthservice.service;

import com.ovah.userauthservice.DTO.AuthResponse;
import com.ovah.userauthservice.DTO.LoginRequest;
import com.ovah.userauthservice.DTO.RegisterRequest;
import com.ovah.userauthservice.config.security.JwtService;
import com.ovah.userauthservice.model.SecurityUser;
import com.ovah.userauthservice.model.User;
import com.ovah.userauthservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.UserRole.USER)
                .build();

        userRepository.save(user);
        log.info("User {} registered", user.getUsername());

        String token = jwtService.generateToken(new SecurityUser(user));

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .build();
    }

    @Transactional
    public AuthResponse authenticate(LoginRequest request) {
        // First try to authenticate
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()
                )
        );

        // Then find user by either username or email
        User user = userRepository.findByEmailOrUsername(
                        request.getUsernameOrEmail(),
                        request.getUsernameOrEmail()
                )
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String token = jwtService.generateToken(new SecurityUser(user));

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .build();
    }
}
