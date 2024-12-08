package com.ovah.userauthservice.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @Email
    private String email;
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}
