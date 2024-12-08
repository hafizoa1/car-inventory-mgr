package com.ovah.userauthservice.service;

import com.ovah.userauthservice.model.User;
import com.ovah.userauthservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Transactional
    public ResponseEntity<User> createUser(User user) {
        User savedUser = userRepository.save(user);
        log.info("Created vehicle with id: {}", savedUser.getId());
        return ResponseEntity.status(201).body(savedUser);
    }

    public ResponseEntity<User> getUser(UUID id) {
        return userRepository.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
