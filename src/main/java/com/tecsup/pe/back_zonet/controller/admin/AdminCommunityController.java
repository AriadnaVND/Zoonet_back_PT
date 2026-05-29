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

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<?> removePost(@PathVariable Long id) {
        moderationService.deletePost(id);
        return ResponseEntity.ok(Map.of("message", "Contenido eliminado exitosamente"));
    }
}