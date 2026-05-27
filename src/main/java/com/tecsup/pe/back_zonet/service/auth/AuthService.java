package com.tecsup.pe.back_zonet.service.auth;

import com.tecsup.pe.back_zonet.dto.admin.LoginRequestDTO;
import com.tecsup.pe.back_zonet.dto.admin.LoginResponseDTO;
import com.tecsup.pe.back_zonet.dto.admin.UserProfileDTO;
import com.tecsup.pe.back_zonet.entity.User;
import com.tecsup.pe.back_zonet.repository.UserRepository;
import com.tecsup.pe.back_zonet.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User register(String name, String email, String password, String plan) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email ya registrado");
        }
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setPlan(plan);
        user.setRole("ROLE_USER"); // Asignación por defecto
        return userRepository.save(user);
    }

    public User login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }
        return user;
    }

    public LoginResponseDTO loginAdmin(LoginRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        // Normalización estricta del rol
        String role = user.getRole() != null ? user.getRole().toUpperCase() : "";
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }

        if (!role.contains("ADMIN")) {
            throw new RuntimeException("Acceso denegado: No cuenta con privilegios de administrador");
        }

        // Generar token con el rol normalizado
        String token = jwtUtils.generateToken(user.getEmail(), role);

        return new LoginResponseDTO(token, user.getEmail(), role);
    }

    public UserProfileDTO getMyProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return new UserProfileDTO(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.isActive());
    }
}