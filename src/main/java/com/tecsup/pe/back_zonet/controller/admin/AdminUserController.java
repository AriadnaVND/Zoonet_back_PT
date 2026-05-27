package com.tecsup.pe.back_zonet.controller.admin;

import com.tecsup.pe.back_zonet.entity.User;
import com.tecsup.pe.back_zonet.service.admin.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "*")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminUserService.obtenerTodosLosUsuarios());
    }

    @PostMapping("/create")
    public ResponseEntity<User> create(@RequestBody User user) {
        return ResponseEntity.ok(adminUserService.crearUsuario(user));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<User> update(@PathVariable Long id, @RequestBody User user) {
        try {
            return ResponseEntity.ok(adminUserService.actualizarUsuario(id, user));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> search(@RequestParam String term) {
        return ResponseEntity.ok(adminUserService.buscarUsuarios(term));
    }

    @PatchMapping("/toggle-active/{id}")
    public ResponseEntity<?> toggleActive(@PathVariable Long id, @RequestParam boolean status) {
        adminUserService.cambiarEstadoUsuario(id, status);
        return ResponseEntity.ok().body(java.util.Map.of("message", "Estado actualizado correctamente"));
    }
}