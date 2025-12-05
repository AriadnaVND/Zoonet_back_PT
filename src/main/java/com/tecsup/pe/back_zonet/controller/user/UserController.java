package com.tecsup.pe.back_zonet.controller.user;

import com.tecsup.pe.back_zonet.entity.User;
import com.tecsup.pe.back_zonet.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// 🔹 IMPORT NUEVO (según lo que pediste)
import com.tecsup.pe.back_zonet.repository.UserRepository;

@RestController
@RequestMapping("/api/user/profile")
public class UserController {

    @Autowired
    private UserService userService;

    // 🔹 NUEVA INYECCIÓN DEL REPOSITORIO
    @Autowired
    private UserRepository userRepository;

    /**
     * 🟢 GET /api/user/profile/{userId}
     * Obtiene los datos del perfil del usuario (nombre, email).
     */
    @GetMapping("/{userId}")
    public ResponseEntity<User> getProfile(@PathVariable Long userId) {
        try {
            User user = userService.getProfile(userId);
            user.setPassword(null);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * 🟢 PUT /api/user/profile/{userId}
     * Actualiza nombre, email y/o contraseña.
     */
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateProfile(
            @PathVariable Long userId,
            @RequestBody Map<String, String> updates) {
        try {
            String name = updates.get("name");
            String email = updates.get("email");
            String password = updates.get("password");

            User updatedUser = userService.updateProfile(userId, name, email, password);
            updatedUser.setPassword(null);

            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * 🟢 DELETE /api/user/profile/{userId}
     * Elimina la cuenta de usuario.
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long userId) {
        try {
            userService.deleteProfile(userId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // ---------------------------------------------------------------
    // 🟢 NUEVO ENDPOINT: Guardar Token FCM para notificaciones PUSH
    // ---------------------------------------------------------------
    /**
     * 🟢 PUT /api/user/profile/{userId}/fcm-token
     * Guarda o actualiza el token FCM del usuario.
     */
    @PutMapping("/{userId}/fcm-token")
    public ResponseEntity<?> updateFcmToken(
            @PathVariable Long userId,
            @RequestBody Map<String, String> body // Recibe {"token": "AAAA..."}
    ) {
        String token = body.get("token");

        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body("El token es obligatorio");
        }

        try {
            User user = userService.getProfile(userId);
            user.setFcmToken(token);
            userRepository.save(user);

            return ResponseEntity.ok(Map.of("message", "Token FCM actualizado correctamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
