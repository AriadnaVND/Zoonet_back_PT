package com.tecsup.pe.back_zonet.controller.auth;

import com.tecsup.pe.back_zonet.dto.AuthRequest;
import com.tecsup.pe.back_zonet.dto.RegisterRequest;
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

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequest request) {
        User user = authService.register(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                request.getPlan() != null ? request.getPlan() : "FREE"
        );

        // 🚫 Ya no se crea la mascota aquí
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody AuthRequest authRequest) {
        User user = authService.login(authRequest.getEmail(), authRequest.getPassword());
        return ResponseEntity.ok(user);
    }
}
