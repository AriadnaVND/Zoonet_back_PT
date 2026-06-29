package com.tecsup.pe.back_zonet.controller.admin;

import com.tecsup.pe.back_zonet.entity.CommunityPost;
import com.tecsup.pe.back_zonet.service.admin.AdminModerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/community")
@CrossOrigin(origins = "*")
public class AdminCommunityController {

    @Autowired
    private AdminModerationService moderationService;

    @GetMapping("/posts")
    public ResponseEntity<List<CommunityPost>> getAllPosts() {
        return ResponseEntity.ok(moderationService.listAllPosts());
    }

    @PostMapping("/posts/{id}/analizar")
    public ResponseEntity<?> analizarPost(@PathVariable Long id) {
        try {
            moderationService.analizarPost(id);
            return ResponseEntity.ok(Map.of("message", "Análisis completado", "postId", id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage())); // ← cambiado
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<?> removePost(@PathVariable Long id) {
        try {
            moderationService.deletePost(id);
            return ResponseEntity.ok(Map.of("message", "Eliminado"));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", "No se pudo eliminar"));
        }
    }
}