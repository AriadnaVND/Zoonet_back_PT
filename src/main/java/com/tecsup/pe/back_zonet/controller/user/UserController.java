package com.tecsup.pe.back_zonet.controller.user;

import com.tecsup.pe.back_zonet.entity.User;
import com.tecsup.pe.back_zonet.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user/profile")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 🟢 GET /api/user/profile/{userId}
     * Obtiene los datos del perfil del usuario (nombre, email).
     */
    @GetMapping("/{userId}")
    public ResponseEntity<User> getProfile(@PathVariable Long userId) {
        try {
            User user = userService.getProfile(userId);
            // IMPORTANTE: Nullificar la contraseña antes de enviarla al cliente
            user.setPassword(null);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }


    /**
     * 🟢 PUT /api/user/profile/{userId}
     * Actualiza nombre, email y/o contraseña.
     * Se usa Map para un request body flexible.
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

            // IMPORTANTE: Nullificar la contraseña antes de enviar la respuesta
            updatedUser.setPassword(null);

            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            // Manejar error de usuario no encontrado o email duplicado
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
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // 204 No Content
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}