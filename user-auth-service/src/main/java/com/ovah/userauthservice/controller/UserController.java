package com.ovah.userauthservice.controller;

import com.ovah.userauthservice.model.User;
import com.ovah.userauthservice.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@AllArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(UUID id){
        return userService.getUser(id);
    }

    @PostMapping
    public User createUsers(){
        return null;
    }


    @PutMapping
    public User editUser(){
        return null;
    }


    @DeleteMapping
    public User deleteUsers(){
        return null;
    }

}
