package com.ovah.userauthservice.controller;

import com.ovah.userauthservice.model.User;
import com.ovah.userauthservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/userController")
public class UserController {

    @Autowired
    UserService userService;


    @GetMapping()
    public ResponseEntity<User> getUser() {
        User user = new User();
        return null;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return null;
    }

    @PutMapping
    public ResponseEntity<User> updateUser(@RequestBody User user) {
        return null;
    }

    @DeleteMapping
    public ResponseEntity<User> deleteUser(@RequestBody User user) {
        return null;
    }
}
