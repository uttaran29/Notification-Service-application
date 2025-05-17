package com.notificationapp.notification.controller;

import com.notificationapp.notification.dto.LoginRequest;
import com.notificationapp.notification.dto.RegisterRequest;
import com.notificationapp.notification.models.User;
import com.notificationapp.notification.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Optional<User> user = userService.login(loginRequest.getEmail(), loginRequest.getPassword());

        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest){
        User user = new User();
        user.setMobile(registerRequest.getMobile());
        user.setPassword(registerRequest.getPassword());
        user.setEmail(registerRequest.getEmail());

        userService.registerUser(user);

        return ResponseEntity.ok("User registered successfully.");
    }
}
