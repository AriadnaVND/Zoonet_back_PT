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

    /**
     * Verifica si el usuario es Free (o si el plan es nulo/desconocido, asumiendo Free)
     */
    public boolean isFreeUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // 🚨 CORRECCIÓN: Manejar si getPlan() devuelve null.
        String plan = user.getPlan();

        // Asume que si el plan es null, es lo mismo que ser FREE (o no premium).
        return plan == null || plan.trim().equalsIgnoreCase("FREE");
    }

    /**
     * Verifica si el usuario es Premium
     */
    public boolean isPremiumUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // 🚨 CORRECCIÓN: Manejar si getPlan() devuelve null.
        String plan = user.getPlan();

        // Solo es Premium si el String es exactamente "PREMIUM"
        return plan != null && plan.trim().equalsIgnoreCase("PREMIUM");
    }
}