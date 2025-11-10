package com.tecsup.pe.back_zonet.service.user;

import com.tecsup.pe.back_zonet.entity.User;
import com.tecsup.pe.back_zonet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Se usa el mismo encoder que en AuthService
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Obtiene el perfil del usuario por ID.
     */
    public User getProfile(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));
    }

    /**
     * Actualiza el perfil del usuario (nombre, email, contraseña).
     * Nota: La actualización del email requiere verificar duplicados.
     */
    @Transactional
    public User updateProfile(Long userId, String name, String email, String password) {
        User user = getProfile(userId);

        // 1. Actualizar Nombre
        if (name != null && !name.trim().isEmpty()) {
            user.setName(name.trim());
        }

        // 2. Actualizar Email (Validar duplicados solo si el email ha cambiado)
        if (email != null && !email.trim().isEmpty() && !email.equalsIgnoreCase(user.getEmail())) {
            if (userRepository.findByEmail(email).isPresent()) {
                throw new RuntimeException("El nuevo email ya está registrado por otro usuario.");
            }
            user.setEmail(email.trim());
        }

        // 3. Actualizar Contraseña (Codificar si se proporciona una nueva)
        if (password != null && !password.isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        }

        return userRepository.save(user);
    }

    /**
     * Elimina el perfil del usuario.
     */
    @Transactional
    public void deleteProfile(Long userId) {
        User user = getProfile(userId);

        // Opcional: Podrías usar user.setActive(false) en lugar de deleteById
        userRepository.deleteById(userId);
    }
}