package com.tecsup.pe.back_zonet.controller.admin;

import com.tecsup.pe.back_zonet.dto.admin.UserSummaryDTO;
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

    // Retorna la lista con la estructura visual completa (Mascota, Collar, Teléfono, etc.)
    @GetMapping("/all")
    public ResponseEntity<List<UserSummaryDTO>> getAllUsers() {
        return ResponseEntity.ok(adminUserService.obtenerResumenUsuariosAdmin());
    }

    // Cambiado para usar el buscador predictivo que procesa el DTO
    @GetMapping("/search")
    public ResponseEntity<List<UserSummaryDTO>> search(@RequestParam String term) {
        return ResponseEntity.ok(adminUserService.buscarResumenUsuarios(term));
    }

    // El botón dinámico de "Suspender / Activar" funciona perfectamente
    @PatchMapping("/toggle-active/{id}")
    public ResponseEntity<?> toggleActive(@PathVariable Long id, @RequestParam boolean status) {
        adminUserService.cambiarEstadoUsuario(id, status);
        return ResponseEntity.ok().body(java.util.Map.of("message", "Estado actualizado correctamente"));
    }
}