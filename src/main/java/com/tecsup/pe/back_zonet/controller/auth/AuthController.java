package com.tecsup.pe.back_zonet.controller.auth;

import com.tecsup.pe.back_zonet.dto.AuthRequest;
import com.tecsup.pe.back_zonet.dto.RegisterRequest;
import com.tecsup.pe.back_zonet.dto.admin.LoginRequestDTO;
import com.tecsup.pe.back_zonet.dto.admin.LoginResponseDTO;
import com.tecsup.pe.back_zonet.dto.admin.UserProfileDTO;
import com.tecsup.pe.back_zonet.entity.User;
import com.tecsup.pe.back_zonet.service.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    // ========================================================
    // 1. ENDPOINTS PARA LA APP MÓVIL (Ruta base: /api/auth)
    // ========================================================

    @PostMapping("/api/auth/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequest request) {
        User user = authService.register(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                request.getPlan() != null ? request.getPlan() : "FREE"
        );
        return ResponseEntity.ok(user);
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<User> login(@RequestBody AuthRequest authRequest) {
        User user = authService.login(authRequest.getEmail(), authRequest.getPassword());
        return ResponseEntity.ok(user);
    }

    // ========================================================
    // 2. ENDPOINTS PARA ADMINISTRACIÓN (Ruta base: /api/admin/auth)
    // ========================================================

    // Ahora la URL será /api/admin/auth/login, capturada perfectamente por el filtro JWT
    @PostMapping("/api/admin/auth/login")
    public ResponseEntity<LoginResponseDTO> loginAdmin(@RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.loginAdmin(request));
    }

    // Ahora la URL será /api/admin/auth/profile para auditar la sesión del administrador
    @GetMapping("/api/admin/auth/profile")
    public ResponseEntity<UserProfileDTO> getProfile(Authentication authentication) {
        String email = authentication.getName(); // Spring Security extrae el email del token JWT
        return ResponseEntity.ok(authService.getMyProfile(email));
    }
}