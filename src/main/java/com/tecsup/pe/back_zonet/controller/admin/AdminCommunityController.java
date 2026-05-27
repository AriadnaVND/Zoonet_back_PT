package com.tecsup.pe.back_zonet.controller.admin;

import com.tecsup.pe.back_zonet.service.admin.AdminModerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/community")
@CrossOrigin(origins = "*")
public class AdminCommunityController {

    @Autowired
    private AdminModerationService moderationService;

    @GetMapping("/posts")
    public ResponseEntity<List<?>> getAllPosts() {
        List<?> posts = moderationService.listAllPosts();
        return ResponseEntity.ok(posts);
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<?> removePost(@PathVariable Long id) {
        moderationService.deletePost(id);
        return ResponseEntity.ok(java.util.Map.of("message", "Contenido eliminado exitosamente"));
    }

    @GetMapping("/ai-audit")
    public ResponseEntity<?> checkAiPerformance() {
        return ResponseEntity.ok(java.util.Map.of("message", "Historial de IA obtenido para auditoría"));
    }
}