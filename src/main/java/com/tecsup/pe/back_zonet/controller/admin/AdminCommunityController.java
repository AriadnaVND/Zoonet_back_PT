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
@CrossOrigin(origins = "*") // Permite la comunicación con tu frontend
public class AdminCommunityController {

    @Autowired
    private AdminModerationService moderationService;

    // Endpoint para listar todos los posts de la comunidad
    @GetMapping("/posts")
    public ResponseEntity<List<CommunityPost>> getAllPosts() {
        return ResponseEntity.ok(moderationService.listAllPosts());
    }

    // Endpoint para disparar el análisis con IA de un post específico
    @PostMapping("/posts/{id}/analizar")
    public ResponseEntity<?> analizarPost(@PathVariable Long id) {
        try {
            // El servicio ahora busca el post y ejecuta la IA
            moderationService.analizarPost(id);
            return ResponseEntity.ok(Map.of(
                    "message", "Análisis completado exitosamente",
                    "postId", id
            ));
        } catch (RuntimeException e) {
            // Manejo de error si el post no existe
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Manejo de errores generales (ej. fallo en conexión con Gemini)
            return ResponseEntity.status(500).body(Map.of("error", "Error interno al procesar IA: " + e.getMessage()));
        }
    }

    // Opcional: Endpoint para eliminar un post
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<?> removePost(@PathVariable Long id) {
        try {
            moderationService.deletePost(id);
            return ResponseEntity.ok(Map.of("message", "Contenido eliminado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", "No se pudo eliminar el post"));
        }
    }
}