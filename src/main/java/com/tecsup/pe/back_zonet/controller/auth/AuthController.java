package com.tecsup.pe.back_zonet.controller.auth;

import com.tecsup.pe.back_zonet.dto.AuthRequest;
import com.tecsup.pe.back_zonet.entity.User;
import com.tecsup.pe.back_zonet.service.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // Registro
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        User createdUser = authService.register(user.getName(), user.getEmail(), user.getPassword(),
                user.getPlan() != null ? user.getPlan() : "FREE");
        return ResponseEntity.ok(createdUser);
    }

    // Login
    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody AuthRequest authRequest) {
        User user = authService.login(authRequest.getEmail(), authRequest.getPassword());
        return ResponseEntity.ok(user);
    }
}
