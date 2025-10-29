package com.example.lmsProject.Controller;

import com.example.lmsProject.dto.LoginRequest;
import com.example.lmsProject.entity.User;
import com.example.lmsProject.security.JwtUtil;
import com.example.lmsProject.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService; // Your UserService
    public AuthController(UserService userService) { this.userService = userService; }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        User user = userService.findByEmail(loginRequest.getEmail());
        if (user != null && user.getPasswordHash().equals(loginRequest.getPassword())) {
            // Normally hash check here; replace with your own password strategy!
            String jwt = JwtUtil.generateToken(user.getEmail());
            return ResponseEntity.ok(Map.of("token", jwt));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }
}

