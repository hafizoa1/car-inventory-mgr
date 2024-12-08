package com.ovah.userauthservice.repository;

import com.ovah.userauthservice.model.User;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    Optional<User> getUserById(UUID id);

    Optional<User> findByEmailOrUsername(String email, String username);
}
