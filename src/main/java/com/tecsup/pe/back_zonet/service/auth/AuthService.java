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

    // Cambiado a PasswordEncoder inyectado para que maneje la desencriptación globalmente
    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==========================================
    // 1. MÉTODOS ORIGINALES (Mantenidos al 100%)
    // ==========================================

    public User register(String name, String email, String password, String plan) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email ya registrado");
        }
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setPlan(plan);
        return userRepository.save(user);
    }

    public User login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }
        return user;
    }

    // ==========================================
    // 2. NUEVOS MÉTODOS DE ADMINISTRACIÓN
    // ==========================================

    public LoginResponseDTO loginAdmin(LoginRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        // Permite validar si viene guardado con prefijo ROLE_ o texto limpio ADMIN
        String userRole = user.getRole() != null ? user.getRole().toUpperCase() : "";
        if (!userRole.contains("ADMIN")) {
            throw new RuntimeException("Acceso denegado: No cuenta con privilegios de administrador");
        }

        String token = jwtUtils.generateToken(user.getEmail(), user.getRole());

        return new LoginResponseDTO(token, user.getEmail(), user.getRole());
    }

    public UserProfileDTO getMyProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Se cambió user.getNombre() por user.getName() para mantener la coherencia con tu entidad
        return new UserProfileDTO(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.isActive());
    }
}