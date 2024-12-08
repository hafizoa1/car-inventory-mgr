package com.ovah.userauthservice.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    private String usernameOrEmail;
    @NotBlank
    private String password;
}

