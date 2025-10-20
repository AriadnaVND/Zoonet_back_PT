package com.tecsup.pe.back_zonet.util;

import com.tecsup.pe.back_zonet.entity.User;
import com.tecsup.pe.back_zonet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RoleValidator {

    private final UserRepository userRepository;

    @Autowired
    public RoleValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Verifica si el usuario es Free
    public boolean isFreeUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return user.getPlan().equalsIgnoreCase("FREE");
    }

    // Verifica si el usuario es Premium
    public boolean isPremiumUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return user.getPlan().equalsIgnoreCase("PREMIUM");
    }
}
